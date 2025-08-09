import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OccupationListComponent } from './occupation-list.component';

describe('OccupationListComponent', () => {
  let component: OccupationListComponent;
  let fixture: ComponentFixture<OccupationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OccupationListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OccupationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
