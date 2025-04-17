package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/voucher")
public class VoucherController {
    @Autowired
    private IVoucherService voucherService;
    @GetMapping("/list/{id}")
    public Result getVoucher(@PathVariable("id")Long id){
        return voucherService.queryVoucherOfShop(id);

    }


}
