import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstoqueDetailsComponent } from './estoque-details.component';

describe('EstoqueDetailsComponent', () => {
  let component: EstoqueDetailsComponent;
  let fixture: ComponentFixture<EstoqueDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstoqueDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstoqueDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
