import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientesDetaisComponent } from './clientes-detais.component';

describe('ClientesDetaisComponent', () => {
  let component: ClientesDetaisComponent;
  let fixture: ComponentFixture<ClientesDetaisComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientesDetaisComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientesDetaisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
