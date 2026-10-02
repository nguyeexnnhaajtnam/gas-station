export interface PageResult<T>{items:T[];page:number;size:number;totalItems:number;hasNext:boolean}
export interface Transaction {id:string;pumpCode:string;fuelName:string;unitPrice:number;liters:number;amount:number;currency:string;completedAt:string;customerName?:string;invoiceState:string;invoiceNumber?:string}
export interface Tank {id:string;name:string;fuelName:string;estimatedVolumeLiters:number;pumpIds:string[]}
export type PumpConnectionStatus='ONLINE'|'OFFLINE'|'UNKNOWN';
export type PumpOperationalStatus='IDLE'|'FUELING'|'MAINTENANCE'|'UNKNOWN';
export type NozzleStatus='HUNG'|'LIFTED'|'DISPENSING'|'UNKNOWN';
export interface PumpRealtime{id:string;number:string;name?:string;fuelType:string;money:number|null;liters:number|null;unitPrice:number|null;totalizer:number|null;connectionStatus:PumpConnectionStatus;operationalStatus:PumpOperationalStatus;nozzleStatus:NozzleStatus;observedAt:string}
export interface DashboardSummary {revenue:number|null;litersSold:number|null;transactionCount:number|null;tankCount:number|null;onlinePumpCount:number|null;pumpOverview:PumpRealtime[];stationContextReady:boolean;priceSourceReady:boolean;partial:boolean;unavailableSources:string[]}
export type CompanyStatus='ACTIVE'|'INACTIVE'|'UNKNOWN';
export interface Company{id:string;name:string;code:string;phone?:string;email?:string;status:CompanyStatus}
export interface CompanyListResponse{items:Company[]}
export interface Station{id:string;companyId:string;code:string;name:string;phone?:string;email?:string}
export interface StationListResponse{items:Station[]}
export interface StationSelectionResponse{station:Station}
