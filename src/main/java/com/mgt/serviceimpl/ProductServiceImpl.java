package com.mgt.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.Product;
import com.mgt.repository.ProductRepo;

@Service
public class ProductServiceImpl  {

	@Autowired
	private ProductRepo productRepo;

	public Product getProductById(int id) {
		return productRepo.findById(id).orElse(null);
	}

	public boolean addPro(Product product, MultipartFile productImage) {
		try {
			// Generate barcode if not already present
			if (product.getProduct_barcode() == null || product.getProduct_barcode().isEmpty()) {
				product.setProduct_barcode("BAR-" + generateAlphanumericCode(8));
			}

			// Store the image and get file path
			String filePath = storeImage(productImage);

			// Save file path to database
			product.setProduct_image(filePath);

			// Save product to database
			productRepo.save(product);

			System.out.println("Product saved successfully.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private String storeImage(MultipartFile file) throws IOException {
		String directory = "C:/MGT/uploads/";
		File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdirs(); // Create directory if not exists
		}

		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		String filePath = directory + fileName;

		// Save file to the server
		file.transferTo(new File(filePath));

		return filePath;
	}

	private String generateAlphanumericCode(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	public List<Product> getAllPro() {
		return productRepo.findAll();
	}

	public Product updatePro(Product product, MultipartFile image) {
		Optional<Product> optionalProduct = productRepo.findById(product.getProduct_id());

		if (optionalProduct.isPresent()) {
			Product existingProduct = optionalProduct.get();

			// Update fields
			existingProduct.setProductName(product.getProductName());
			existingProduct.setProduct_price(product.getProduct_price());
			existingProduct.setProduct_category(product.getProduct_category());
			existingProduct.setProduct_available_stock_quantity(product.getProduct_available_stock_quantity());
			existingProduct.setProduct_description(product.getProduct_description());
			existingProduct.setGst_rate(product.getGst_rate());
			existingProduct.setGst_type(product.getGst_type());

			// Update image if a new one is provided
			if (image != null && !image.isEmpty()) {
				try {
					String imagePath = storeImage(image);
					existingProduct.setProduct_image(imagePath);
				} catch (IOException e) {
					throw new RuntimeException("Failed to save image", e);
				}
			}

			return productRepo.save(existingProduct);
		} else {
			throw new RuntimeException("Product not found with id: " + product.getProduct_id());
		}
	}

	public boolean deletePro(Integer product_id) {
		boolean status = false;
		if (product_id != null) {
			productRepo.deleteById(product_id);
			status = true;
		}
		return status;
	}
}
