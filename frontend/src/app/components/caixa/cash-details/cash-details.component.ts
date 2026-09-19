import { Component } from '@angular/core';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { MdbRippleModule } from 'mdb-angular-ui-kit/ripple';

@Component({
  selector: 'app-cash-details',
  imports: [ MdbFormsModule, MdbRippleModule],
  templateUrl: './cash-details.component.html',
  styleUrl: './cash-details.component.scss'
})
export class CashDetailsComponent {

}
