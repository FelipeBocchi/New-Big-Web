import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SaleEditorComponent } from './sale-editor.component';

describe('SaleEditorComponent', () => {
  let component: SaleEditorComponent;
  let fixture: ComponentFixture<SaleEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SaleEditorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SaleEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
