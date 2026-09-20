import { Component } from '@angular/core';
import { CashDetailsComponent } from '../cash-details/cash-details.component';

@Component({
  selector: 'app-cash-master',
  imports: [CashDetailsComponent],
  templateUrl: './cash-master.component.html',
  styleUrl: './cash-master.component.scss'
})
export class CashMasterComponent {

}
