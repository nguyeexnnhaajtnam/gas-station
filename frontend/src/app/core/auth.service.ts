import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
export type AuthenticationStatus='AUTHENTICATED'|'REJECTED'|'UNVERIFIED';
export interface LoginResponse{status:AuthenticationStatus;message:string}
@Injectable({providedIn:'root'})
export class AuthService{
  private readonly http=inject(HttpClient);private readonly api=environment.apiBaseUrl;
  readonly authenticated=signal(false);
  login(username:string,password:string){return this.http.post<LoginResponse>(`${this.api}/api/v1/auth/login`,{username,password}).pipe(tap(r=>this.authenticated.set(r.status==='AUTHENTICATED')));}
  logout(){this.authenticated.set(false);}
}
