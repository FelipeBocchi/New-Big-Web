import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CashPanelComponent } from './cash-panel.component';

describe('CashPanelComponent', () => {
  let component: CashPanelComponent;
  let fixture: ComponentFixture<CashPanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CashPanelComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CashPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
