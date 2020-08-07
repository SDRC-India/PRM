import { TestBed, async, inject } from '@angular/core/testing';

import { RoleGuardGuard } from './role-guard.guard';

describe('RoleGuardGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RoleGuardGuard]
    });
  });

  it('should ...', inject([RoleGuardGuard], (guard: RoleGuardGuard) => {
    expect(guard).toBeTruthy();
  }));
});
