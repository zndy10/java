package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {
    ServiceResponse saveOrUpdateProduct(Product product);

    ServiceResponse<String> setSaleStatus(Integer productId, Integer status);

    ServiceResponse<ProductDetailVo>  manageProductDetail(Integer productId);

    ServiceResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    ServiceResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);


    // 前端实现service
    ServiceResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServiceResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy);
}
