package com.shopflow.service;

import com.shopflow.dto.request.CouponRequest;
import com.shopflow.dto.response.CouponResponse;
import com.shopflow.entity.Coupon;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Code promo déjà existant : " + request.getCode());
        }
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .type(request.getType())
                .valeur(request.getValeur())
                .dateExpiration(request.getDateExpiration())
                .usagesMax(request.getUsagesMax())
                .build();
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        coupon.setType(request.getType());
        coupon.setValeur(request.getValeur());
        coupon.setDateExpiration(request.getDateExpiration());
        coupon.setUsagesMax(request.getUsagesMax());
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        coupon.setActif(false);
        couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public CouponResponse validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException("Code promo invalide"));

        if (!coupon.isActif()) throw new BusinessException("Ce coupon est inactif");
        if (coupon.getDateExpiration() != null && coupon.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Ce coupon a expiré");
        }
        if (coupon.getUsagesMax() != null && coupon.getUsagesActuels() >= coupon.getUsagesMax()) {
            throw new BusinessException("Ce coupon a atteint son nombre maximum d'utilisations");
        }

        return toResponse(coupon);
    }

    private CouponResponse toResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType())
                .valeur(coupon.getValeur())
                .dateExpiration(coupon.getDateExpiration())
                .usagesMax(coupon.getUsagesMax())
                .usagesActuels(coupon.getUsagesActuels())
                .actif(coupon.isActif())
                .build();
    }
}
