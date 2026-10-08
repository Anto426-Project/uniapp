const {test} = require('node:test');
const assert = require('node:assert/strict');
const resolve = require('../resolve_publish_source.cjs');

function fixture(platform = 'linux', overrides = {}) {
  const outputs = {};
  const run = {id: 123, workflow_id: 1, head_branch: 'main', event: 'workflow_dispatch',
    head_repository: {full_name: 'owner/uniapp'}, head_sha: 'a'.repeat(40), status: 'completed', conclusion: 'success', ...overrides};
  const artifacts = [{id: 11, name: `${platform}-build-123-linux`, expired: false},
    {id: 12, name: `${platform}-build-123-archlinux`, expired: false}];
  const actions = {listWorkflowRuns: async () => ({data: {workflow_runs: [run]}}),
    getWorkflowRun: async () => ({data: run}),
    getWorkflow: async () => ({data: {path: `.github/workflows/build-${platform}.yml`}}),
    listWorkflowRunArtifacts() {}};
  const options = {platform, requestedRunId: '123', context: {repo: {owner: 'owner', repo: 'uniapp'}},
    core: {setOutput: (key, value) => {outputs[key] = value;}},
    github: {rest: {actions}, paginate: async () => artifacts}, sleep: async () => {}};
  return {options, outputs, run, actions, artifacts};
}

test('successful Linux build resolves only its own package IDs', async () => {
  const f = fixture();
  f.artifacts.push({id: 13, name: 'linux-tests-123', expired: false});
  await resolve(f.options);
  assert.equal(f.outputs.artifact_ids, '11,12');
  assert.equal(f.outputs.platform, 'linux');
});

test('failed, cancelled and pending builds cannot publish', async () => {
  for (const override of [{conclusion: 'failure'}, {conclusion: 'cancelled'}, {status: 'in_progress', conclusion: null}]) {
    const f = fixture('linux', override);
    await assert.rejects(resolve(f.options), /completed successful/);
    assert.deepEqual(f.outputs, {});
  }
});

test('dispatch race waits until the exact source run completes', async () => {
  const f = fixture('linux', {status: 'in_progress', conclusion: null});
  let waited = false;
  f.options.sleep = async () => {waited = true; f.run.status = 'completed'; f.run.conclusion = 'success';};
  await resolve(f.options);
  assert.equal(waited, true);
  assert.equal(f.outputs.run_id, '123');
});

test('forks, foreign workflows, non-main and pull request runs are rejected', async () => {
  for (const override of [{head_branch: 'feature'}, {event: 'pull_request'}, {head_repository: {full_name: 'fork/uniapp'}}]) {
    const f = fixture('linux', override);
    await assert.rejects(resolve(f.options), /trusted main-branch/);
  }
  const f = fixture();
  f.actions.getWorkflow = async () => ({data: {path: '.github/workflows/build-windows.yml'}});
  await assert.rejects(resolve(f.options), /trusted main-branch/);
});

test('legacy desktop recovery keeps Windows and Linux packages separate', async () => {
  for (const [platform, expected] of [['windows', '13'], ['linux', '11,12']]) {
    const f = fixture(platform);
    f.actions.getWorkflow = async () => ({data: {path: '.github/workflows/build-desktop.yml'}});
    f.options.github.paginate = async () => ['linux', 'archlinux', 'windows', 'macos'].map((os, i) =>
      ({id: i + 11, name: `desktop-build-123-${os}`, expired: false}));
    await resolve(f.options);
    assert.equal(f.outputs.artifact_ids, expected);
  }
});

test('expired packages are rejected rather than publishing an incomplete release', async () => {
  const f = fixture();
  f.artifacts[1].expired = true;
  await assert.rejects(resolve(f.options), /missing or expired/);
});

test('latest build lookup uses the selected workflow and successful conclusion', async () => {
  const f = fixture();
  f.options.requestedRunId = '';
  f.actions.listWorkflowRuns = async (args) => {
    assert.equal(args.workflow_id, 'build-linux.yml');
    assert.equal(args.status, 'success');
    return {data: {workflow_runs: [f.run]}};
  };
  await resolve(f.options);
});
