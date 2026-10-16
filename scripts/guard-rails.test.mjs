import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, it } from 'node:test';
import { fileURLToPath } from 'node:url';

const GUARD = fileURLToPath(new URL('../.claude/hooks/guard.py', import.meta.url));

// Strings that the guard looks for are assembled at runtime, so this file itself stays harmless.
const DISABLED = '@' + 'Disabled';
const SKIP = 'it' + '.skip(';

function project() {
  const root = mkdtempSync(join(tmpdir(), 'guard-'));
  mkdirSync(join(root, 'docs/requirements'), { recursive: true });
  mkdirSync(join(root, 'backend/src/test/java'), { recursive: true });
  return root;
}

function runGuard(root, tool_name, tool_input) {
  const result = spawnSync('python3', [GUARD], {
    input: JSON.stringify({ cwd: root, tool_name, tool_input }),
    encoding: 'utf8',
  });
  return { blocked: result.status === 2, message: result.stderr };
}

const requirement = (status, criteria = ['AC-1', 'AC-2']) =>
  `---\nid: REQ-900\nstatus: ${status}\n---\n\n## Acceptance criteria\n` +
  criteria.map((ac) => `- **${ac}** Something.`).join('\n') +
  '\n';

describe('[NFR-007/AC-5] PreToolUse guard rail', () => {
  it('NFR-007/AC-5 blocks disabling a JUnit test', () => {
    const root = project();
    const file = join(root, 'backend/src/test/java/FooTest.java');
    writeFileSync(file, 'class FooTest {\n  @Test void works() {}\n}\n');

    const result = runGuard(root, 'Edit', {
      file_path: file,
      old_string: '  @Test',
      new_string: `  ${DISABLED}\n  @Test`,
    });

    assert.equal(result.blocked, true);
    assert.match(result.message, /must not be disabled/);
  });

  it('NFR-007/AC-5 blocks skipping a frontend test', () => {
    const root = project();
    const file = join(root, 'backend/src/main/webui/src/app/app.spec.ts');

    const result = runGuard(root, 'Write', {
      file_path: file,
      content: `${SKIP}'does things', () => {});`,
    });

    assert.equal(result.blocked, true);
  });

  it('NFR-007/AC-5 allows ordinary test edits', () => {
    const root = project();
    const file = join(root, 'backend/src/test/java/FooTest.java');
    writeFileSync(file, 'class FooTest {}\n');

    const result = runGuard(root, 'Edit', {
      file_path: file,
      old_string: '{}',
      new_string: '{ @Test void more() {} }',
    });

    assert.equal(result.blocked, false);
  });

  it('NFR-007/AC-5 blocks deleting test files', () => {
    const root = project();

    for (const command of [
      'rm backend/src/test/java/FooTest.java',
      'git rm -q e2e/tests/login.spec.ts',
      'cd x && rm -f scripts/trace.test.mjs',
    ]) {
      assert.equal(runGuard(root, 'Bash', { command }).blocked, true, command);
    }
    assert.equal(runGuard(root, 'Bash', { command: 'rm -rf backend/target' }).blocked, false);
  });

  it('NFR-007/AC-5 blocks force pushes and pushes to main', () => {
    const root = project();

    assert.equal(runGuard(root, 'Bash', { command: 'git push --force' }).blocked, true);
    assert.equal(runGuard(root, 'Bash', { command: 'git push origin main' }).blocked, true);
    assert.equal(
      runGuard(root, 'Bash', { command: 'git push -u origin feat/REQ-001-login' }).blocked,
      false,
    );
  });

  it('NFR-007/AC-5 blocks removing criteria from an approved requirement', () => {
    const root = project();
    const file = join(root, 'docs/requirements/REQ-900-example.md');
    writeFileSync(file, requirement('approved'));

    const result = runGuard(root, 'Write', { file_path: file, content: requirement('approved', ['AC-1']) });

    assert.equal(result.blocked, true);
    assert.match(result.message, /AC-2 must not be removed/);
  });

  it('NFR-007/AC-5 blocks moving an approved requirement back to draft', () => {
    const root = project();
    const file = join(root, 'docs/requirements/REQ-900-example.md');
    writeFileSync(file, requirement('approved'));

    const result = runGuard(root, 'Edit', {
      file_path: file,
      old_string: 'status: approved',
      new_string: 'status: draft',
    });

    assert.equal(result.blocked, true);
  });

  it('NFR-007/AC-5 only a human may approve a requirement', () => {
    const root = project();
    const file = join(root, 'docs/requirements/REQ-900-example.md');
    writeFileSync(file, requirement('draft'));

    const result = runGuard(root, 'Edit', {
      file_path: file,
      old_string: 'status: draft',
      new_string: 'status: approved',
    });

    assert.equal(result.blocked, true);
  });

  it('NFR-007/AC-5 allows marking an approved requirement as implemented', () => {
    const root = project();
    const file = join(root, 'docs/requirements/REQ-900-example.md');
    writeFileSync(file, requirement('approved'));

    const result = runGuard(root, 'Edit', {
      file_path: file,
      old_string: 'status: approved',
      new_string: 'status: implemented',
    });

    assert.equal(result.blocked, false);
  });
});
