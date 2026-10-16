// Structural checks for requirements that are about the repository itself (stack, layout, CI files).
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { describe, it } from 'node:test';
import { fileURLToPath } from 'node:url';

const ROOT = fileURLToPath(new URL('..', import.meta.url));
const read = (path) => readFileSync(join(ROOT, path), 'utf8');
const exists = (path) => existsSync(join(ROOT, path));

describe('[NFR-001] technology stack and layout', () => {
  const pom = read('backend/pom.xml');

  it('NFR-001/AC-1 backend is Quarkus 3.x on Java 21 with the Maven Wrapper', () => {
    assert.match(pom, /<quarkus\.platform\.version>3\.\d+\.\d+<\/quarkus\.platform\.version>/);
    assert.match(pom, /<maven\.compiler\.release>21<\/maven\.compiler\.release>/);
    assert.ok(exists('backend/mvnw'));
    assert.ok(exists('backend/.mvn/wrapper/maven-wrapper.properties'));
  });

  it('NFR-001/AC-2 backend uses the required Quarkus extensions', () => {
    for (const extension of [
      'quarkus-rest-jackson',
      'quarkus-hibernate-orm-panache',
      'quarkus-jdbc-postgresql',
      'quarkus-flyway',
      'quarkus-oidc',
      'quarkus-smallrye-openapi',
      'quarkus-smallrye-health',
      'quarkus-container-image-jib',
      'quarkus-jacoco',
      'quarkus-quinoa',
    ]) {
      assert.match(pom, new RegExp(`<artifactId>${extension}</artifactId>`), extension);
    }
  });

  it('NFR-001/AC-3 frontend is a current Angular app with Material in the Quinoa folder', () => {
    const pkg = JSON.parse(read('backend/src/main/webui/package.json'));
    const major = Number(/\d+/.exec(pkg.dependencies['@angular/core'])[0]);
    assert.ok(major >= 21, `Angular ${major}`);
    assert.ok(pkg.dependencies['@angular/material']);
    assert.ok(exists('backend/src/main/webui/angular.json'));
  });

  it('NFR-001/AC-6 schema changes are Flyway migrations and Hibernate does not generate the schema', () => {
    assert.ok(exists('backend/src/main/resources/db/migration/V1__baseline.sql'));
    assert.match(
      read('backend/src/main/resources/application.properties'),
      /^quarkus\.hibernate-orm\.schema-management\.strategy=none$/m,
    );
  });

  it('NFR-001/AC-7 the API client is generated from the OpenAPI document', () => {
    const pkg = JSON.parse(read('backend/src/main/webui/package.json'));
    assert.match(pkg.scripts['generate:api'], /openapi-generator-cli generate .*\/q\/openapi/);
  });

  it('NFR-001/AC-8 the repository has the agreed top-level layout', () => {
    for (const path of ['backend', 'e2e', 'docs', 'scripts', '.github', 'CLAUDE.md']) {
      assert.ok(exists(path), path);
    }
  });

  it('NFR-001/AC-9 the README documents the rootless Podman setup', () => {
    const readme = read('README.md');
    for (const step of [
      'podman-docker',
      'systemctl --user enable --now podman.socket',
      'DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock',
      'TESTCONTAINERS_RYUK_DISABLED=true',
    ]) {
      assert.ok(readme.includes(step), step);
    }
  });
});

describe('[NFR-005] CI/CD configuration', () => {
  it('NFR-005/AC-1 ci.yml runs verify and the trace check on PRs and pushes to main', () => {
    const ci = read('.github/workflows/ci.yml');
    assert.match(ci, /pull_request:/);
    assert.match(ci, /push:\s*\n\s*branches: \[main\]/);
    assert.match(ci, /\.\/mvnw -B -ntp verify/);
    assert.match(ci, /node scripts\/trace\.mjs/);
  });

  it('NFR-005/AC-2 e2e.yml runs Playwright on PRs and keeps report and traces', () => {
    const e2e = read('.github/workflows/e2e.yml');
    assert.match(e2e, /pull_request:\s*\n\s*branches: \[main\]/);
    assert.match(e2e, /npx playwright test/);
    assert.match(e2e, /playwright-report/);
  });

  it('NFR-005/AC-3 release.yml pushes a Jib image to GHCR and creates a release on v* tags', () => {
    const release = read('.github/workflows/release.yml');
    assert.match(release, /tags: \['v\*'\]/);
    assert.match(release, /quarkus\.container-image\.push=true/);
    assert.match(release, /gh release create/);
    assert.match(read('backend/src/main/resources/application.properties'), /registry=ghcr\.io/);
  });

  it('NFR-005/AC-4 CodeQL analyses Java and TypeScript on PRs and weekly', () => {
    const codeql = read('.github/workflows/codeql.yml');
    assert.match(codeql, /java-kotlin/);
    assert.match(codeql, /javascript-typescript/);
    assert.match(codeql, /pull_request:/);
    assert.match(codeql, /cron:/);
  });

  it('NFR-005/AC-5 Dependabot covers Maven, npm and GitHub Actions', () => {
    const dependabot = read('.github/dependabot.yml');
    for (const ecosystem of ['maven', 'npm', 'github-actions']) {
      assert.match(dependabot, new RegExp(`package-ecosystem: ${ecosystem}`), ecosystem);
    }
  });

  it('NFR-005/AC-8 PR template with Definition of Done and issue templates exist', () => {
    assert.match(read('.github/pull_request_template.md'), /Definition of Done/);
    assert.ok(exists('.github/ISSUE_TEMPLATE/requirement.md'));
    assert.ok(exists('.github/ISSUE_TEMPLATE/bug.md'));
  });
});

describe('[NFR-007] guard rail configuration', () => {
  const settings = JSON.parse(read('.claude/settings.json'));

  it('NFR-007/AC-3 CLAUDE.md contains the Definition of Done', () => {
    const claude = read('CLAUDE.md');
    assert.match(claude, /## Definition of Done/);
    assert.match(claude, /status` is set to `implemented`/);
  });

  it('NFR-007/AC-4 permissions allow the build tools and deny force pushes, main and secrets', () => {
    const { allow, deny } = settings.permissions;
    for (const rule of ['Bash(./mvnw:*)', 'Bash(npm:*)', 'Bash(npx:*)', 'Bash(podman:*)', 'Bash(gh pr:*)']) {
      assert.ok(allow.includes(rule), rule);
    }
    for (const rule of ['Bash(git push --force:*)', 'Bash(git push origin main:*)', 'Read(./.env)', 'Read(./deploy/secrets/**)']) {
      assert.ok(deny.includes(rule), rule);
    }
  });

  it('NFR-007/AC-5 formatter, guard and stop hooks are registered', () => {
    const commands = JSON.stringify(settings.hooks);
    assert.match(commands, /hooks\/guard\.py/);
    assert.match(commands, /hooks\/format\.py/);
    assert.match(commands, /hooks\/stop_check\.py/);
    assert.ok(settings.hooks.PreToolUse && settings.hooks.PostToolUse && settings.hooks.Stop);
  });

  it('NFR-007/AC-6 the three subagents are defined, the reviewer read-only', () => {
    for (const agent of ['requirements-analyst', 'test-author', 'reviewer']) {
      assert.match(read(`.claude/agents/${agent}.md`), new RegExp(`^name: ${agent}$`, 'm'));
    }
    const reviewerTools = /^tools: (.*)$/m.exec(read('.claude/agents/reviewer.md'))[1];
    assert.doesNotMatch(reviewerTools, /\b(Write|Edit)\b/);
  });
});
