const {test} = require('node:test');
const assert = require('node:assert/strict');
const {classify} = require('./triage-issue.cjs');
const body = '### Problema\nLa schermata si blocca\n### Passaggi per riprodurlo\n1. Apri il libretto\n### Comportamento atteso\nCaricamento completato\n### Versioni\n2.0.1 (201)\n### Piattaforma\nAndroid';
test('complete issue gets a platform label without needs-info', () => {
  assert.deepEqual(classify({title:'[Bug]: caricamento', labels:[], body}), {wanted:['platform:android'], missing:[]});
});
test('empty form sections and no-response placeholders need information', () => {
  const result = classify({title:'problem', labels:[{name:'bug'}], body:'### Problema\n_No response_\n### Passaggi per riprodurlo\n1. \n'});
  assert.equal(result.missing.length, 4);
  assert.deepEqual(result.wanted, ['needs-info']);
});
test('legacy markdown headings remain supported', () => {
  assert.equal(classify({title:'[Bug]', labels:[], body:body.replaceAll('###', '##')}).missing.length, 0);
});
test('unrelated issues are not classified as bugs', () => {
  assert.equal(classify({title:'Idea', labels:[], body}), null);
});
test('code and shell-looking text are treated only as text', () => {
  const hostile = body.replace('La schermata si blocca', '$(touch /tmp/should-not-exist) ${{ secrets.TOKEN }} `process.exit(1)`');
  assert.equal(classify({title:'[Bug]', labels:[], body:hostile}).missing.length, 0);
});
