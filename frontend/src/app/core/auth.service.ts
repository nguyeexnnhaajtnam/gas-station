import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
export type AuthenticationStatus='AUTHENTICATED'|'REJECTED'|'UNVERIFIED';
export interface LoginResponse{status:AuthenticationStatus;message:string;accessToken?:string}
/** Access token is kept in memory only (never persisted/logged); a reload requires logging in again. */
@Injectable({providedIn:'root'})
export class AuthService{
  private readonly http=inject(HttpClient);private readonly api=environment.apiBaseUrl;
  private readonly accessToken=signal<string|null>(null);
  readonly authenticated=computed(()=>!!this.accessToken());
  token(){return this.accessToken();}
  login(username:string,password:string){return this.http.post<LoginResponse>(`${this.api}/api/v1/auth/login`,{username,password}).pipe(tap(r=>this.accessToken.set(r.status==='AUTHENTICATED'&&r.accessToken?r.accessToken:null)));}
  logout(){
    if(this.accessToken())this.http.post(`${this.api}/api/v1/auth/logout`,null).subscribe({error:()=>{}});
    this.clear();
  }
  /** Drops the local token without calling the server (e.g. after a 401). */
  clear(){this.accessToken.set(null);}
}
