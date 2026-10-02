import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, exhaustMap, finalize, merge, Subject, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { CompanyContextService } from '../../core/company-context.service';
import { PumpRealtime } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../shared/components/search-field/search-field.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { LucideFuel, LucideRefreshCw, LucideWifi, LucideWifiOff } from '../../shared/icons';

type Filter = 'ALL'|'ONLINE'|'OFFLINE'|'DISPENSING'|'IDLE';
@Component({standalone:true,imports:[DatePipe,DecimalPipe,PageHeaderComponent,SearchFieldComponent,EmptyStateComponent,ErrorStateComponent,SkeletonComponent,LucideFuel,LucideRefreshCw,LucideWifi,LucideWifiOff],changeDetection:ChangeDetectionStrategy.OnPush,templateUrl:'./pump-monitoring.component.html',styleUrl:'./pump-monitoring.component.scss'})
export class PumpMonitoringComponent {
  private api=inject(ApiService);
  private readonly destroyRef=inject(DestroyRef);
  private readonly manualRefresh=new Subject<void>();
  readonly context=inject(CompanyContextService);
  readonly loading=signal(true); readonly error=signal(''); readonly pumps=signal<PumpRealtime[]>([]);
  readonly updated=signal<Date|null>(null); readonly query=signal(''); readonly filter=signal<Filter>('ALL');
  readonly filters:[Filter,string][]=[['ALL','Tất cả'],['ONLINE','Online'],['OFFLINE','Offline'],['DISPENSING','Đang bơm'],['IDLE','Rảnh']];
  readonly visible=computed(()=>{const q=this.query().trim().toLocaleLowerCase('vi'),f=this.filter();return this.pumps().filter(p=>(!q||[p.number,p.name,p.fuelType].some(v=>v?.toLocaleLowerCase('vi').includes(q)))&&(f==='ALL'||f===p.connectionStatus||(f==='DISPENSING'&&p.operationalStatus==='FUELING')||(f==='IDLE'&&p.operationalStatus==='IDLE')))});
  readonly online=computed(()=>this.pumps().filter(p=>p.connectionStatus==='ONLINE').length);
  readonly offline=computed(()=>this.pumps().filter(p=>p.connectionStatus==='OFFLINE').length);
  readonly dispensing=computed(()=>this.pumps().filter(p=>p.operationalStatus==='FUELING').length);
  constructor(){
    merge(timer(0,5_000),this.manualRefresh).pipe(
      exhaustMap(()=>{
        this.loading.set(this.pumps().length===0);
        this.error.set('');
        return this.api.pumpsRealtime().pipe(
          catchError(()=>{
            this.error.set('Không thể tải trạng thái trụ bơm. Vui lòng thử lại.');
            return EMPTY;
          }),
          finalize(()=>this.loading.set(false)),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(p=>{this.pumps.set(p);this.updated.set(new Date())});
  }
  load(){this.manualRefresh.next()}
  connectionLabel(p:PumpRealtime){return p.connectionStatus==='ONLINE'?'Online':p.connectionStatus==='OFFLINE'?'Offline':'Chưa xác định'}
  statusLabel(p:PumpRealtime){return p.operationalStatus==='FUELING'?'Đang bơm':p.operationalStatus==='IDLE'?'Rảnh':p.operationalStatus==='MAINTENANCE'?'Bảo trì':'Chưa xác định'}
}
