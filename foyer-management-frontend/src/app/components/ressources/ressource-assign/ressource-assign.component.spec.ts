import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RessourceAssignComponent } from './ressource-assign.component';

describe('RessourceAssignComponent', () => {
  let component: RessourceAssignComponent;
  let fixture: ComponentFixture<RessourceAssignComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RessourceAssignComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RessourceAssignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
