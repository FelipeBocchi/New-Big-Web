import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CashMasterComponent } from './cash-master.component';

describe('CashMasterComponent', () => {
  let component: CashMasterComponent;
  let fixture: ComponentFixture<CashMasterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CashMasterComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CashMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
