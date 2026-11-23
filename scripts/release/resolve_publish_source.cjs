// Resolve only successful builds from the repository's main branch.
const workflows = {
  android: 'build-android.yml', ios: 'build-ios.yml',
};

module.exports = async function resolve({github, context, core, platform, requestedRunId,
  sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))}) {
  if (!workflows[platform]) throw new Error('Unsupported application platform');
  let id = requestedRunId;
  if (!id) {
    const {data} = await github.rest.actions.listWorkflowRuns({
      ...context.repo, workflow_id: workflows[platform], branch: 'main',
      event: 'workflow_dispatch', status: 'success', per_page: 1,
    });
    id = String(data.workflow_runs[0]?.id || '');
  }
  if (!/^\d+$/.test(id)) throw new Error('A successful build run ID is required');
  let {data: run} = await github.rest.actions.getWorkflowRun({...context.repo, run_id: Number(id)});
  const {data: workflow} = await github.rest.actions.getWorkflow({...context.repo, workflow_id: run.workflow_id});
  if (workflow.path !== `.github/workflows/${workflows[platform]}` ||
      run.head_branch !== 'main' || run.head_repository?.full_name !== `${context.repo.owner}/${context.repo.repo}` ||
      run.event !== 'workflow_dispatch') {
    throw new Error('Only a trusted main-branch build for the selected platform can be published');
  }
  // Publication is explicitly dispatched by the build's final job. Wait for that
  // source run to close before accepting its result; no workflow_run chain is needed.
  for (let attempt = 0; run.status !== 'completed' && attempt < 30; attempt++) {
    await sleep(10000);
    ({data: run} = await github.rest.actions.getWorkflowRun({...context.repo, run_id: Number(id)}));
  }
  if (run.status !== 'completed' || run.conclusion !== 'success') {
    throw new Error('Publication requires a completed successful build');
  }
  const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts,
    {...context.repo, run_id: Number(id), per_page: 100});
  const name = `${platform}-build-${id}`;
  const selected = artifacts.filter((artifact) => artifact.name === name);
  if (!selected.length || selected.some((artifact) => artifact.expired)) {
    throw new Error('The selected run has missing or expired build packages');
  }
  for (const [key, value] of Object.entries({run_id: id, sha: run.head_sha, platform,
    artifact_ids: selected.map((artifact) => artifact.id).join(',')})) core.setOutput(key, value);
};
