package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    // CONNEXION (à ajouter)
    LoginResponse login(LoginRequest request);

    // Nouvelles méthodes Mot de passe oublié et reinintialisation du mot de passe
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
    ResetPasswordResponse resetPassword(ResetPasswordRequest request);
}
