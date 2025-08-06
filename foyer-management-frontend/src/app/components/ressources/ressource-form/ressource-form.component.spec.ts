import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RessourceFormComponent } from './ressource-form.component';

describe('RessourceFormComponent', () => {
  let component: RessourceFormComponent;
  let fixture: ComponentFixture<RessourceFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RessourceFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RessourceFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
