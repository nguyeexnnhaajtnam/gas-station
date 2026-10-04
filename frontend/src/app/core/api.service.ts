import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CompanyListResponse, Customer, DashboardSummary, FuelPrice, PumpCodeHistory, PumpCodeHistorySearchRequest, PumpColumn, PumpRealtime, RevenueReport, SpringPage, StationListResponse, StationSelectionResponse, StoreDetails, Tank } from './api.models';
import { environment } from '../../environments/environment';
@Injectable({providedIn:'root'})
export class ApiService {
  private http=inject(HttpClient);
  private readonly api=environment.apiBaseUrl;
  dashboard(){return this.http.get<DashboardSummary>(`${this.api}/api/v1/dashboard/summary`);}
  pumpCodeHistory(search:PumpCodeHistorySearchRequest){
    let params=new HttpParams().set('page',search.page).set('size',search.size);
    for(const [key,value] of Object.entries(search)) if(key!=='page'&&key!=='size'&&value) params=params.set(key,String(value));
    return this.http.get<SpringPage<PumpCodeHistory>>(`${this.api}/api/v1/pump-codes/history`,{params});
  }
  tanks(){return this.http.get<Tank[]>(`${this.api}/api/v1/tanks`);}
  pumpsRealtime(){return this.http.get<PumpRealtime[]>(`${this.api}/api/v1/pumps/realtime`);}
  pumpColumns(){return this.http.get<PumpColumn[]>(`${this.api}/api/v1/pump-columns`);}
  fuelPrices(){return this.http.get<FuelPrice[]>(`${this.api}/api/v1/fuel-prices`);}
  storeInfo(){return this.http.get<StoreDetails>(`${this.api}/api/v1/store-info`);}
  customers(){return this.http.get<Customer[]>(`${this.api}/api/v1/customers`);}
  revenueReport(from:string,to:string){return this.http.get<RevenueReport>(`${this.api}/api/v1/revenue-report`,{params:{from,to}});}
  companies(){return this.http.get<CompanyListResponse>(`${this.api}/api/v1/companies`);}
  stations(companyId:string){return this.http.get<StationListResponse>(`${this.api}/api/v1/companies/${encodeURIComponent(companyId)}/stations`);}
  selectStation(stationId:string){return this.http.post<StationSelectionResponse>(`${this.api}/api/v1/stations/${encodeURIComponent(stationId)}/select`,{});}
}
