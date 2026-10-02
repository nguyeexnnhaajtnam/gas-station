import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CompanyListResponse, DashboardSummary, PageResult, PumpRealtime, StationListResponse, StationSelectionResponse, Tank, Transaction } from './api.models';
import { environment } from '../../environments/environment';
@Injectable({providedIn:'root'})
export class ApiService {
  private http=inject(HttpClient);
  private readonly api=environment.apiBaseUrl;
  dashboard(){return this.http.get<DashboardSummary>(`${this.api}/api/v1/dashboard/summary`);}
  transactions(page=0,size=20){return this.http.get<PageResult<Transaction>>(`${this.api}/api/v1/transactions`,{params:new HttpParams().set('page',page).set('size',size)});}
  tanks(){return this.http.get<Tank[]>(`${this.api}/api/v1/tanks`);}
  pumpsRealtime(){return this.http.get<PumpRealtime[]>(`${this.api}/api/v1/pumps/realtime`);}
  companies(){return this.http.get<CompanyListResponse>(`${this.api}/api/v1/companies`);}
  stations(companyId:string){return this.http.get<StationListResponse>(`${this.api}/api/v1/companies/${encodeURIComponent(companyId)}/stations`);}
  selectStation(stationId:string){return this.http.post<StationSelectionResponse>(`${this.api}/api/v1/stations/${encodeURIComponent(stationId)}/select`,{});}
}
