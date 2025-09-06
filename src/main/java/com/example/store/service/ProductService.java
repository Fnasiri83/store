package com.example.store.service;

import com.example.store.dto.ProductAttributeValueCreateDTO;
import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.mapper.ProductAttributeValueMapper;
import com.example.store.mapper.ProductMapper;
import com.example.store.model.Category;
import com.example.store.model.Product;
import com.example.store.model.ProductAttributeValue;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.ProductAttributeValueRepository;
import com.example.store.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final ProductAttributeValueService productAttributeValueService;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    private static final String UPLOAD_DIR = "uploads/";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    @Transactional
    public ProductDTO uploadFile(Long productId, MultipartFile file) {
        log.info("آپلود فایل برای محصول با شناسه: {}", productId);

        // اعتبارسنجی محصول
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("محصول با شناسه " + productId + " پیدا نشد"));

        // اعتبارسنجی نوع فایل
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidFileExtension(originalFilename)) {
            log.error("نوع فایل غیرمجاز: {}", originalFilename);
            throw new IllegalArgumentException("فقط فایل‌های JPG, PNG یا GIF مجاز هستند");
        }

        try {
            // ذخیره فایل
            String filename = UUID.randomUUID() + "_" + originalFilename;
            Path path = Paths.get(UPLOAD_DIR + filename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // اضافه کردن مسیر فایل به محصول
            product.getFileUrls().add("/uploads/" + filename);
            Product saved = productRepository.save(product);

            log.info("فایل ذخیره شد: مسیر={}", "/uploads/" + filename);
            return toDTO(saved);

        } catch (IOException e) {
            log.error("خطا در ذخیره فایل: {}", e.getMessage());
            throw new RuntimeException("خطا در آپلود فایل", e);
        }
    }
    @Transactional
    public ProductDTO deleteFile(Long productId, String fileUrl) {
        log.info("حذف فایل با مسیر: {} از محصول با شناسه: {}", fileUrl, productId);

        // اعتبارسنجی محصول
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("محصول با شناسه " + productId + " پیدا نشد"));

        // حذف مسیر فایل از لیست
        if (!product.getFileUrls().remove(fileUrl)) {
            log.error("فایل با مسیر {} یافت نشد", fileUrl);
            throw new IllegalArgumentException("فایل با مسیر " + fileUrl + " در محصول یافت نشد");
        }

        // حذف فایل از سرور
        try {
            Path path = Paths.get(fileUrl.substring(1)); // حذف "/" اولیه
            Files.deleteIfExists(path);
            log.info("فایل از سرور حذف شد: {}", fileUrl);
        } catch (IOException e) {
            log.error("خطا در حذف فایل از سرور: {}", e.getMessage());
            // ادامه می‌دیم حتی اگه فایل روی سرور حذف نشه
        }

        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    private boolean isValidFileExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setFileUrls(product.getFileUrls());
        return dto;
    }

    /**
     * ایجاد محصول جدید
     */
    public ProductDTO create(ProductCreateDTO dto) {
        log.info("Received ProductCreateDTO: {}", dto);

        // 1. تبدیل DTO به Product
        Product product = productMapper.toEntity(dto);

        // 2. اطمینان از تنظیم category
        Category category = categoryRepository.findById(Long.valueOf(dto.getCategoryId()))
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        product.setCategory(category);

        System.out.println("عشق کن");

        // 3. اطمینان از مقداردهی اولیه attributeValues
        if (product.getAttributeValues() == null) {
            product.setAttributeValues(new ArrayList<>());
        }
        System.out.println(" که  داره کار میکنه برنامت");
        // 4. پردازش و ذخیره ویژگی‌ها قبل از ذخیره اصلی
        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
            product.getAttributeValues().clear(); // پاک کردن لیست موجود

            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
                ProductAttributeValue pav = productAttributeValueMapper.toEntity(attrDTO);
                pav.setProduct(product); // تنظیم رابطه دوطرفه
                product.getAttributeValues().add(pav);
            }
        }

        // 5. ذخیره Product (به همراه attributeValues به دلیل cascade)
        Product savedProduct = productRepository.save(product);

        // 6. تبدیل به DTO و بازگشت
        return productMapper.toDTO(savedProduct);
    }

    /**
     * دریافت همه محصولات
     */
    public List<ProductDTO> getAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * دریافت محصول بر اساس ID
     */
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return productMapper.toDTO(product);
    }

    /**
     * بروزرسانی محصول
     */
    public ProductDTO update(Long id, ProductCreateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        return productMapper.toDTO(productRepository.save(product));
    }

    /**
     * حذف محصول
     */
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found");
        }
        productAttributeValueRepository.deleteByProductId(id); // حذف وابستگی‌ها
        productRepository.deleteById(id);
    }
}

//    public void delete(Long id) {
//        if (!productRepository.existsById(id)) {
//            throw new EntityNotFoundException("Product not found");
//        }
//        productRepository.deleteById(id);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        log.info("Received ProductCreateDTO: {}", dto);
//
//        // 1. تبدیل DTO به Product
//        Product product = productMapper.toEntity(dto);
//
//        // 2. اطمینان از تنظیم category
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // 3. اطمینان از مقداردهی اولیه attributeValues
//        if (product.getAttributeValues() == null) {
//            product.setAttributeValues(new ArrayList<>());
//        }
//
// 4. ذخیره اولیه Product
//        Product savedProduct = productRepository.save(product);
//
//        // 5. پردازش و ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            // پاک کردن لیست موجود (به جای جایگزینی مرجع)
//            savedProduct.getAttributeValues().clear();
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                log.info("Processing attribute: {}", attrDTO);
//                ProductAttributeValueDTO pavDTO = productAttributeValueService.create(savedProduct.getId(), attrDTO);
//                ProductAttributeValue pav = productAttributeValueRepository.findById(pavDTO.getId())
//                        .orElseThrow(() -> new RuntimeException("Failed to retrieve ProductAttributeValue"));
//                savedProduct.getAttributeValues().add(pav); // اضافه کردن به لیست موجود
//            }
//            // 6. ذخیره مجدد Product برای به‌روزرسانی رابطه
//            savedProduct = productRepository.save(savedProduct);
//        }
//
//        // 7. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        log.info("Received ProductCreateDTO: {}", dto);
//
//        // 1. تبدیل DTO به Product
//        Product product = productMapper.toEntity(dto);
//
//        // 2. اطمینان از تنظیم category
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // 3. تنظیم لیست attributeValues خالی
//        product.setAttributeValues(new ArrayList<>());
//
//        // 4. ذخیره اولیه Product
//        Product savedProduct = productRepository.save(product);
//
//        // 5. پردازش و ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            List<ProductAttributeValue> attributeValues = new ArrayList<>();
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                log.info("Processing attribute: {}", attrDTO);
//                ProductAttributeValueDTO pavDTO = productAttributeValueService.create(savedProduct.getId(), attrDTO);
//                ProductAttributeValue pav = productAttributeValueRepository.findById(pavDTO.getId())
//                        .orElseThrow(() -> new RuntimeException("Failed to retrieve ProductAttributeValue"));
//                attributeValues.add(pav);
//            }
//            savedProduct.setAttributeValues(attributeValues);
//            // 6. ذخیره مجدد Product برای به‌روزرسانی رابطه
//            savedProduct = productRepository.save(savedProduct);
//        }
//
//        // 7. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        log.info("Received ProductCreateDTO: {}", dto);
//
//        // 1. تبدیل DTO به Product
//        Product product = productMapper.toEntity(dto);
//
//        // 2. اطمینان از تنظیم category
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // 3. ذخیره اولیه Product
//        Product savedProduct = productRepository.save(product);
//
//        // 4. پردازش و ذخیره ویژگی‌ها و اضافه کردن به لیست attributeValues
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            List<ProductAttributeValue> attributeValues = new ArrayList<>();
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                ProductAttributeValueDTO pavDTO = productAttributeValueService.create(savedProduct.getId(), attrDTO);
//                ProductAttributeValue pav = productAttributeValueRepository.findById(pavDTO.getId())
//                        .orElseThrow(() -> new RuntimeException("Failed to retrieve ProductAttributeValue"));
//                attributeValues.add(pav);
//            }
//            savedProduct.setAttributeValues(attributeValues);
//            // 5. ذخیره مجدد Product برای به‌روزرسانی رابطه
//            savedProduct = productRepository.save(savedProduct);
//        }
//
//        // 6. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        // 1. تبدیل DTO به Product و ذخیره آن
//        Product product = productMapper.toEntity(dto);
//        Product savedProduct = productRepository.save(product);
//
//        // 2. پردازش و ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                productAttributeValueService.create(savedProduct.getId(), attrDTO);
//            }
//        }
//
//        // 3. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    @Transactional
//    public ProductDTO create(ProductCreateDTO dto) {
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        Product product = productMapper.toEntity(dto);
//        product.setCategory(category); // تنظیم category
//        return productMapper.toDTO(productRepository.save(product));
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        Product product = productMapper.toEntity(dto);
//        return productMapper.toDTO(productRepository.save(product));
//    }
//    private static final String UPLOAD_DIR = "uploads/";


//    /**
//    اپلود عکس*
//    */
//    public ProductDTO addProduct(ProductCreateDTO dto, MultipartFile photo) throws IOException {
//        Product product = productMapper.toEntity(dto);
//
//        // ذخیره عکس
//        if (photo != null && !photo.isEmpty()) {
//            String filename = UUID.randomUUID() + "_" + photo.getOriginalFilename();
//            Path path = Paths.get("uploads/" + filename);
//            Files.createDirectories(path.getParent());
//            Files.write(path, photo.getBytes());
//            product.setPhotoUrl("/uploads/" + filename);
//        }
//
//        product = productRepository.save(product);
//
//        // ذخیره AttributeValues
//        List<ProductAttributeValue> values = dto.getAttributeValues().stream()
//                .map(pavMapper::toEntity)
//                .peek(val -> val.setProduct(product)) // اتصال به product
//                .collect(Collectors.toList());
//
//        pavRepository.saveAll(values);
//        product.setAttributeValues(values);
//
//        return productMapper.toDTO(product);
//    }
//    @Transactional
//    public Product createProduct(ProductCreateDTO dto, MultipartFile photo) {
//        log.info("Creating product with title: {}", dto.getTitle());
//
//        // اعتبارسنجی دسته‌بندی
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
//
//        // ایجاد محصول
//        Product product = new Product();
//        product.setTitle(dto.getTitle());
//        product.setDescription(dto.getDescription());
//        product.setPrice(dto.getPrice());
//        product.setStock(dto.getStock());
//        product.setCondition(dto.getCondition());
//        product.setCategory(category);
//
//        // ذخیره فایل عکس (در صورت وجود)
//        if (photo != null && !photo.isEmpty()) {
//            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
//            try {
//                Path uploadPath = Paths.get(UPLOAD_DIR);
//                if (!Files.exists(uploadPath)) {
//                    Files.createDirectories(uploadPath);
//                }
//                Files.write(Paths.get(UPLOAD_DIR + fileName), photo.getBytes());
//                product.setPhotoPath(fileName);
//            } catch (IOException e) {
//                log.error("Failed to save photo: {}", e.getMessage());
//                throw new RuntimeException("Failed to save photo", e);
//            }
//        }
//
//        // ذخیره محصول
//        Product savedProduct = productRepository.save(product);
//
//        // ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null) {
//            for (AttributeValueDTo attrDto : dto.getAttributeValues()) {
//                Attribute attribute = attributeRepository.findById(attrDto.getAttributeId())
//                        .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + attrDto.getAttributeId()));
//
//                ProductAttribute productAttribute = new ProductAttribute();
//                productAttribute.setProduct(savedProduct);
//                productAttribute.setAttribute(attribute);
//                productAttribute.setValue(attrDto.getValue());
//                productAttributeRepository.save(productAttribute);
//            }
//        }
//
//        log.info("Product created with id: {}", savedProduct.getId());
//        return savedProduct;
//    }
//