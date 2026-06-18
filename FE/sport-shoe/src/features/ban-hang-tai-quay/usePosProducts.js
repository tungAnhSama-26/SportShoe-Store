import { computed, ref, watch } from "vue";
import { timSanPhamTaiQuay } from "../../services/ban-hang-tai-quay";
import { GUEST_LABEL } from "./constants";
import { extractProductKeywordFromQr } from "./qr-product";

export function usePosProducts({
  soLuongConLai,
  themSanPham,
  clearFeedback,
  pageError,
  successMessage
}) {
  const productKeyword = ref("");
  const productVariantResults = ref([]);
  const selectedProductDetail = ref(null);
  const selectedColor = ref("");
  const selectedSize = ref("");
  const selectedQuantity = ref(1);
  const loadingProducts = ref(false);
  const showProductDropdown = ref(false);
  
  const currentPage = ref(1);
  const pageSize = ref(5);

  let productTimer;

  const productSearchLabel = computed(
    () => productKeyword.value.trim() ? "Kết quả tìm kiếm sản phẩm" : "Sản phẩm tại quầy"
  );
  const selectedBrandFilter = ref("");
  const selectedCategoryFilter = ref("");

  const availableBrands = computed(() => {
    const brands = new Set();
    productVariantResults.value.forEach(p => {
      if (p.thuongHieu) brands.add(p.thuongHieu);
    });
    return Array.from(brands).sort();
  });

  const availableCategories = computed(() => {
    const categories = new Set();
    productVariantResults.value.forEach(p => {
      if (p.loaiGiay) categories.add(p.loaiGiay);
    });
    return Array.from(categories).sort();
  });

  const productResults = computed(() => {
    let filtered = productVariantResults.value;
    
    if (selectedBrandFilter.value) {
      filtered = filtered.filter(p => p.thuongHieu === selectedBrandFilter.value);
    }
    
    if (selectedCategoryFilter.value) {
      filtered = filtered.filter(p => p.loaiGiay === selectedCategoryFilter.value);
    }
    
    return groupProductVariants(filtered);
  });

  const totalItems = computed(() => productResults.value.length);
  const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value) || 1);
  
  watch([selectedBrandFilter, selectedCategoryFilter, productKeyword], () => {
    currentPage.value = 1;
  });
  
  const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    return productResults.value.slice(start, start + pageSize.value);
  });

  const relatedVariants = computed(() => {
    if (!selectedProductDetail.value) {
      return [];
    }
    return productVariantResults.value.filter(
      (product) => product.maSanPham === selectedProductDetail.value?.maSanPham &&
        product.tenSanPham === selectedProductDetail.value?.tenSanPham
    );
  });
  const colorOptions = computed(() => {
    const grouped = new Map();
    for (const variant of relatedVariants.value) {
      const key = variant.mauSac || variant.maBienThe;
      if (!grouped.has(key)) {
        grouped.set(key, variant);
      }
    }
    return Array.from(grouped.values());
  });
  const sizeOptions = computed(
    () => relatedVariants.value.filter((variant) => {
      if (!selectedColor.value) {
        return true;
      }
      return (variant.mauSac || variant.maBienThe) === selectedColor.value;
    })
  );
  const selectedVariant = computed(() => {
    if (!selectedProductDetail.value) {
      return null;
    }
    return relatedVariants.value.find(
      (variant) => (
        selectedColor.value
          ? (variant.mauSac || variant.maBienThe) === selectedColor.value
          : true
      ) && (
        selectedSize.value
          ? (variant.kichCo || "") === selectedSize.value
          : true
      )
    ) || selectedProductDetail.value;
  });
  const chiTietDangChon = computed(() => selectedVariant.value || selectedProductDetail.value);
  const hinhAnhDangChon = computed(
    () => chiTietDangChon.value?.hinhAnh || selectedProductDetail.value?.hinhAnh || ""
  );
  const soLuongTonKhaDungChiTiet = computed(() => {
    if (!chiTietDangChon.value) {
      return 0;
    }
    return soLuongConLai(chiTietDangChon.value.chiTietId, chiTietDangChon.value.soLuongTon);
  });

  const soLuongTonSauKhiChon = computed(
    () => Math.max(soLuongTonKhaDungChiTiet.value - selectedQuantity.value, 0)
  );


  async function fetchProducts(keyword) {
    loadingProducts.value = true;
    try {
      productVariantResults.value = await timSanPhamTaiQuay(keyword);
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể tìm sản phẩm";
    } finally {
      loadingProducts.value = false;
    }
  }

  function groupProductVariants(products) {
    const grouped = new Map();

    for (const product of products) {
      const key = `${product.maSanPham}::${product.tenSanPham}`;
      const soLuongKhaDung = soLuongConLai(product.chiTietId, product.soLuongTon);

      if (!grouped.has(key)) {
        grouped.set(key, {
          ...product,
          soLuongTon: 0,
          tongBienThe: 0,
          coGiamGia: false
        });
      }

      const groupedProduct = grouped.get(key);
      groupedProduct.soLuongTon += soLuongKhaDung;
      groupedProduct.tongBienThe += 1;
      groupedProduct.coGiamGia =
        groupedProduct.coGiamGia ||
        Number(product.giaBan || 0) < Number(product.giaGoc || 0);
      if (!groupedProduct.hinhAnh && product.hinhAnh) {
        groupedProduct.hinhAnh = product.hinhAnh;
      }
    }

    return Array.from(grouped.values());
  }

  function timBienTheChinhXacTheoMa(products, keyword) {
    const normalizedKeyword = String(keyword ?? "").trim().toLowerCase();
    if (!normalizedKeyword) {
      return null;
    }

    return products.find((product) =>
      [product.maBienThe, product.chiTietId]
        .map((value) => String(value ?? "").trim().toLowerCase())
        .some((value) => value && value === normalizedKeyword)
    ) ?? null;
  }

  function laySoLuongTonHienTai(chiTietId, fallback) {
    return productVariantResults.value.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
  }

  function moChiTietSanPham(product) {
    selectedProductDetail.value = product;
    selectedColor.value = product.mauSac || product.maBienThe;
    selectedSize.value = product.kichCo || "";
    selectedQuantity.value = 1;
  }

  function dongChiTietSanPham() {
    selectedProductDetail.value = null;
    selectedColor.value = "";
    selectedSize.value = "";
    selectedQuantity.value = 1;
  }

  async function handleProductQrScan(rawValue) {
    clearFeedback();

    const keyword = extractProductKeywordFromQr(rawValue);
    if (!keyword) {
      pageError.value = "Không đọc được mã QR sản phẩm";
      return;
    }

    clearProductTimer();
    productKeyword.value = keyword;
    showProductDropdown.value = false;
    loadingProducts.value = true;

    try {
      const products = await timSanPhamTaiQuay(keyword);
      productVariantResults.value = products;

      if (!products.length) {
        pageError.value = `Không tìm thấy sản phẩm với mã ${keyword}`;
        return;
      }

      const exactVariant = timBienTheChinhXacTheoMa(products, keyword);
      if (exactVariant) {
        const isAdded = themSanPham(exactVariant, 1, {
          preserveProductSearch: true,
          scannedKeyword: keyword,
          scannedProducts: products,
        });
        if (isAdded) {
          successMessage.value =
            `Đã quét và thêm ${exactVariant.tenSanPham} - ${exactVariant.mauSac} / ${exactVariant.kichCo}. Bạn có thể quét tiếp để tăng số lượng.`;
        }
        return;
      }

      const groupedProducts = groupProductVariants(products);
      if (groupedProducts.length === 1) {
        moChiTietSanPham(groupedProducts[0]);
        successMessage.value = `Đã nhận mã ${keyword}. Chọn biến thể phù hợp để thêm vào giỏ.`;
        return;
      }

      showProductDropdown.value = true;
      successMessage.value = `Đã quét mã ${keyword}. Chọn sản phẩm phù hợp trong danh sách.`;
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể quét sản phẩm lúc này";
    } finally {
      loadingProducts.value = false;
    }
  }

  function chonMauSac(value) {
    selectedColor.value = value;
    selectedSize.value = sizeOptions.value[0]?.kichCo || "";
    selectedQuantity.value = 1;
  }

  function chonKichCo(value) {
    selectedSize.value = value;
    selectedQuantity.value = 1;
  }

  function giamSoLuongChiTiet() {
    selectedQuantity.value = Math.max(selectedQuantity.value - 1, 1);
  }

  function tangSoLuongChiTiet() {
    if (!selectedVariant.value) {
      return;
    }
    const soLuongToiDa = soLuongConLai(selectedVariant.value.chiTietId, selectedVariant.value.soLuongTon);
    selectedQuantity.value = Math.min(selectedQuantity.value + 1, Math.max(soLuongToiDa, 1));
  }

  function moDanhSachSanPham() {
    showProductDropdown.value = true;
    return fetchProducts(productKeyword.value);
  }

  function dongDanhSachSanPham() {
    window.setTimeout(() => {
      showProductDropdown.value = false;
    }, 150);
  }

  function clearProductTimer() {
    if (productTimer) {
      window.clearTimeout(productTimer);
    }
  }

  watch(productKeyword, (value) => {
    clearProductTimer();
    currentPage.value = 1;
    showProductDropdown.value = value.trim().length > 0;
    productTimer = window.setTimeout(() => {
      void fetchProducts(value);
    }, 250);
  });

  return {
    productKeyword,
    productVariantResults,
    selectedProductDetail,
    selectedColor,
    selectedSize,
    selectedQuantity,
    loadingProducts,
    showProductDropdown,
    productSearchLabel,
    productResults,
    paginatedProducts,
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    selectedBrandFilter,
    selectedCategoryFilter,
    availableBrands,
    availableCategories,
    relatedVariants,
    colorOptions,
    sizeOptions,
    selectedVariant,
    chiTietDangChon,
    hinhAnhDangChon,
    soLuongTonKhaDungChiTiet,
    soLuongTonSauKhiChon,
    fetchProducts,
    groupProductVariants,
    timBienTheChinhXacTheoMa,
    laySoLuongTonHienTai,
    moChiTietSanPham,
    dongChiTietSanPham,
    handleProductQrScan,
    chonMauSac,
    chonKichCo,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    moDanhSachSanPham,
    dongDanhSachSanPham,
    clearProductTimer
  };
}
