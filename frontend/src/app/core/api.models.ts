export interface PageResult<T>{items:T[];page:number;size:number;totalItems:number;hasNext:boolean}
export interface SpringPage<T>{content:T[];number:number;size:number;totalElements:number;totalPages:number;first:boolean;last:boolean;empty:boolean}
export interface PumpCodeHistory {pumpCode:string;dispenser:string;fuelType:string;unitPrice:number;volume:number;amount:number;finishedAt:string;customer?:string;invoiceStatus:string;invoiceNumber?:string}
export interface PumpCodeHistorySearchRequest {from?:string;to?:string;pumpId?:string;fuelType?:string;customer?:string;amountFilter?:string;volumeFilter?:string;status?:string;sort?:string;page:number;size:number}
export interface PumpColumn {id:string;name:string;fuelType:string;deviceAddress?:string;displayName?:string;serialNumber?:string;connectionStatus:'ONLINE'|'OFFLINE'|'UNKNOWN';totalizer:number|null}
export interface FuelPrice {id:string;fuelName:string;unitPrice:number|null;effectiveAt:string|null;updateStatus:'UPDATED'|'SCHEDULED'|'UNKNOWN'}
export interface StoreDetails {companyName?:string;companyAddress?:string;taxCode?:string;companyPhone?:string;companyFax?:string;companyEmail?:string;storeName?:string;storeAddress?:string;storePhone?:string;storeFax?:string;storeEmail?:string}
export interface Customer {id:string;name:string;taxCode?:string;address?:string;email?:string}
export interface RevenueBreakdown {name:string;liters:number;revenue:number;count:number}
export interface RevenueReport {from:string;to:string;todayRevenue:number;todayLiters:number;todayPumpCodeCount:number;todayInvoiceRevenue:number;todayInvoiceLiters:number;todayInvoiceCount:number;fuels:RevenueBreakdown[];pumps:RevenueBreakdown[];invoices:RevenueBreakdown[]}
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
