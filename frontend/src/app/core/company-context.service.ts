import { Injectable, signal } from '@angular/core';
import { Company, Station } from './api.models';
@Injectable({providedIn:'root'})
export class CompanyContextService {
  readonly selectedCompany=signal<Company|null>(null);
  readonly selectedStation=signal<Station|null>(null);
  select(company:Company){this.selectedCompany.set(company);this.selectedStation.set(null);}
  selectStation(station:Station){this.selectedStation.set(station);}
  clear(){this.selectedCompany.set(null);this.selectedStation.set(null);}
}
