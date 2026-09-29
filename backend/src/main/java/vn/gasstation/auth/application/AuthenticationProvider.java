package vn.gasstation.auth.application;
import vn.gasstation.auth.domain.AuthenticationStatus;
public interface AuthenticationProvider { AuthenticationStatus authenticate(String username, String password); }
