import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { evaluate, findReferences, parseRequirement, render } from './trace.mjs';

// Fixture IDs are assembled at runtime so the trace check does not count them as real references.
const R = ['REQ', '042'].join('-');

const requirement = (status, extra = '') => `---
id: ${R}
title: Example
type: functional
status: ${status}
priority: must
milestone: M1
---

## Acceptance criteria
- **AC-1** Given a thing, then it works.
- **AC-2** Given another thing, then it also works.
${extra}`;

describe('[NFR-007/AC-2] trace check', () => {
  it('NFR-007/AC-2 parses front matter and acceptance criteria', () => {
    const req = parseRequirement(requirement('approved', '- **AC-3** Checked by hand. _(manual)_'));

    assert.equal(req.id, R);
    assert.equal(req.status, 'approved');
    assert.equal(req.milestone, 'M1');
    assert.deepEqual(req.criteria, [
      { ac: 1, manual: false },
      { ac: 2, manual: false },
      { ac: 3, manual: true },
    ]);
  });

  it('NFR-007/AC-1 finds references in test names and tags', () => {
    const refs = findReferences(
      `@DisplayName("${R}/AC-1 works")\ndescribe('[${R}/AC-2] and NFR-001/AC-9')`,
    );

    assert.deepEqual(refs, [
      { id: R, ac: 1, line: 1 },
      { id: R, ac: 2, line: 2 },
      { id: 'NFR-001', ac: 9, line: 2 },
    ]);
  });

  it('NFR-004/AC-9 fails for an implemented requirement with an untested criterion', () => {
    const req = parseRequirement(requirement('implemented'));
    const refs = [{ id: R, ac: 1, line: 1, file: 'a.java' }];

    const { problems } = evaluate([req], refs);

    assert.deepEqual(problems, [`${R}/AC-2 is implemented but has no test`]);
  });

  it('NFR-007/AC-2 only reports gaps of requirements that are not implemented yet', () => {
    const { problems } = evaluate([parseRequirement(requirement('approved'))], []);

    assert.deepEqual(problems, []);
  });

  it('NFR-007/AC-2 manual criteria do not need a test', () => {
    const req = parseRequirement(requirement('implemented', '- **AC-3** By hand. _(manual)_'));
    const refs = [1, 2].map((ac) => ({ id: R, ac, line: ac, file: 'a.java' }));

    assert.deepEqual(evaluate([req], refs).problems, []);
  });

  it('NFR-007/AC-2 fails for references to unknown criteria', () => {
    const req = parseRequirement(requirement('approved'));
    const refs = [{ id: R, ac: 7, line: 3, file: 'b.ts' }];

    assert.deepEqual(evaluate([req], refs).problems, [
      `b.ts:3 references unknown criterion ${R}/AC-7`,
    ]);
  });

  it('NFR-007/AC-2 renders a summary and a detail row per criterion', () => {
    const req = parseRequirement(requirement('approved'));
    const { rows } = evaluate([req], [{ id: R, ac: 1, line: 5, file: 'x.java' }]);

    const markdown = render([req], rows);

    assert.ok(markdown.includes(`| ${R} Example | approved | M1 | 1/2 |`));
    assert.ok(markdown.includes(`| ${R}/AC-1 | \`x.java:5\` |`));
    assert.ok(markdown.includes(`| ${R}/AC-2 | — |`));
  });
});
