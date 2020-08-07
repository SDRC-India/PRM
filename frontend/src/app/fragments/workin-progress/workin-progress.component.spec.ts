import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkinProgressComponent } from './workin-progress.component';

describe('WorkinProgressComponent', () => {
  let component: WorkinProgressComponent;
  let fixture: ComponentFixture<WorkinProgressComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkinProgressComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkinProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
