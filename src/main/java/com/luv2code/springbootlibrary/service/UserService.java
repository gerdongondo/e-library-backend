package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.UpdateProfileRequest;
import com.luv2code.springbootlibrary.dto.UpdateProfileResponse;
import com.luv2code.springbootlibrary.dto.UserProfileResponse;

public interface UserService {
    /**
     * Récupère le profil de l'utilisateur actuellement connecté
     * @return UserProfileResponse contenant les informations de l'utilisateur
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Met à jour le profil de l'utilisateur actuellement connecté
     * @param request DTO contenant les nouvelles informations
     * @return UpdateProfileResponse avec les informations mises à jour
     */
    UpdateProfileResponse updateCurrentUserProfile(UpdateProfileRequest request);

}