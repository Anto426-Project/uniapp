// Issue text is data: never execute it or interpolate it into shell commands.
const managedLabels = ['platform:android', 'platform:ios', 'platform:web', 'needs-info'];

function section(body, title) {
  const escaped = title.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const value = body.match(new RegExp(`^#{2,3} ${escaped}\\s*\\n([\\s\\S]*?)(?=^#{2,3} |$(?![\\s\\S]))`, 'mi'))?.[1] || '';
  return value.replace(/<!--[\s\S]*?-->/g, '').replace(/_No response_|^\s*\d+\.\s*$/gmi, '').trim();
}

function classify(issue) {
  const labels = issue.labels.map(label => typeof label === 'string' ? label : label.name);
  if (!labels.includes('bug') && !/^\[bug\]/i.test(issue.title)) return null;
  const body = issue.body || '';
  const platform = section(body, 'Piattaforma').toLowerCase();
  const missing = ['Problema', 'Passaggi per riprodurlo', 'Comportamento atteso', 'Versioni']
    .filter(title => !section(body, title));
  const wanted = [];
  if (platform.startsWith('android')) wanted.push('platform:android');
  if (platform.startsWith('ios')) wanted.push('platform:ios');
  if (platform.startsWith('sito')) wanted.push('platform:web');
  if (missing.length) wanted.push('needs-info');
  return {wanted, missing};
}

async function triage({github, context, core}) {
  const issue = context.payload.issue;
  const result = classify(issue);
  if (!result || issue.state !== 'open') return;
  const repo = context.repo;
  const current = issue.labels.map(label => typeof label === 'string' ? label : label.name);
  const wanted = [...result.wanted, 'bug'];
  if (['opened', 'reopened'].includes(context.payload.action)) wanted.push('needs-triage');
  const definitions = {
    bug: ['d73a4a', 'Problema segnalato'], 'needs-triage': ['fbca04', 'Da verificare'],
    'needs-info': ['d4c5f9', 'Informazioni necessarie alla riproduzione mancanti'],
    'platform:android': ['3ddc84', 'Android'], 'platform:ios': ['a2aaad', 'iOS'],
    'platform:web': ['0366d6', 'Sito di distribuzione'],
  };
  for (const name of wanted) {
    try { await github.rest.issues.getLabel({...repo, name}); }
    catch (error) {
      if (error.status !== 404) throw error;
      const [color, description] = definitions[name];
      await github.rest.issues.createLabel({...repo, name, color, description});
    }
  }
  for (const name of managedLabels.filter(name => current.includes(name) && !wanted.includes(name))) {
    await github.rest.issues.removeLabel({...repo, issue_number: issue.number, name});
  }
  await github.rest.issues.addLabels({...repo, issue_number: issue.number, labels: wanted});
  // One idempotent comment: edits update it instead of adding notification noise.
  const marker = '<!-- uniapp-issue-triage -->';
  const comments = await github.paginate(github.rest.issues.listComments, {...repo, issue_number: issue.number, per_page: 100});
  const existing = comments.find(comment => comment.user?.login === 'github-actions[bot]' && comment.body?.includes(marker));
  if (result.missing.length) {
    const body = `${marker}\nPer verificare il problema, completa queste sezioni: **${result.missing.join(', ')}**.\n\nNon pubblicare credenziali o dati personali. Questo controllo verifica solo la presenza delle informazioni; la riproducibilità sarà valutata durante la revisione.`;
    if (existing && existing.body !== body) await github.rest.issues.updateComment({...repo, comment_id: existing.id, body});
    else if (!existing) await github.rest.issues.createComment({...repo, issue_number: issue.number, body});
  } else if (existing) {
    await github.rest.issues.deleteComment({...repo, comment_id: existing.id});
  }
  core.info(`Issue #${issue.number}: ${wanted.join(', ')}`);
}
module.exports = triage;
module.exports.classify = classify;
