import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BulkUserRegistrationComponent } from './bulk-user-registration.component';

describe('BulkUserRegistrationComponent', () => {
  let component: BulkUserRegistrationComponent;
  let fixture: ComponentFixture<BulkUserRegistrationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BulkUserRegistrationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BulkUserRegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
