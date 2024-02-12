package com.cognifyz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import com.cognifyz.models.Product;
import com.cognifyz.models.ProductDto;
import com.cognifyz.services.ProductRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

	@Autowired
	private ProductRepository repo;

	@GetMapping({ "", "/" })
	public String showProductList(Model model) {

		List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);
		return "products/index";
	}

	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/CreateProduct";
	}

	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {
		if (productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}

		if (result.hasErrors()) {
			return "products/CreateProduct";
		}

		// save the image file
		MultipartFile image = productDto.getImageFile();
		Date createdAt = new Date();
		String storageFileName = createdAt.getTime() + "- " + image.getOriginalFilename();

		try {
			String uploadDir = "public/images/";
			java.nio.file.Path uploadPath = Paths.get(uploadDir);

			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			try (InputStream inputStream = image.getInputStream()) {
				Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ex) {
			System.out.println("Exception :  " + ex.getMessage());
		}

		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreatedAt(createdAt);
		product.setImageName(storageFileName);

		repo.save(product);
		return "redirect:/products";
	}

	@GetMapping("/edit")
	public String showeditPage(Model model, @RequestParam int id) {

		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);

			ProductDto productDto = new ProductDto();
			productDto.setName(product.getName());
			productDto.setBrand(product.getBrand());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDescription());

			model.addAttribute("productDto", productDto);

		} catch (Exception ex) {
			System.out.println("Exception : " + ex.getMessage());
			return "redirect:/products";
		}

		return "products/EditProduct";
	}

	@PostMapping("/edit")
	public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto,
			BindingResult result) {

		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);
			
			if (result.hasErrors()) {
				return "products/EditProduct";
			}

			if (!productDto.getImageFile().isEmpty()) {
				// delete old file
				String uploadDir = "public/images/";
				java.nio.file.Path oldImagepath = Paths.get(uploadDir + product.getImageName());
				
				try {
					Files.delete(oldImagepath);
				}
				catch(Exception ex)
				{
					System.out.println("Exception : "+ex.getMessage());
				}
				
				//save the file
				MultipartFile image= productDto.getImageFile();
				Date createdAt = new Date();
				String storageFileName= createdAt.getTime() + "_" + image.getOriginalFilename();
				try(InputStream inputStream = image.getInputStream())
				{
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),StandardCopyOption.REPLACE_EXISTING);
				}
				product.setImageName(storageFileName);
			}
			
			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());
			
			repo.save(product);

		}

		catch (Exception ex) {
			System.out.println("Exception : " + ex.getMessage());
		}

		return "redirect:/products";
	}
	
	
	@GetMapping("/delete")
	public String deleteProduct(@RequestParam int id) {
		
		try {
			Product product = repo.findById(id).get();
			
			//delete product Image
			Path imagePath= Paths.get("public/images/" +product.getImageName());
			try
			{
				Files.delete(imagePath);
			}
			catch(Exception ex)
			{
				System.out.println("Exception : " +ex.getMessage() );
			}
			
			//delete the product
			repo.delete(product);
		}catch (Exception ex) {
			System.out.println("Exception : "+ex.getMessage());
		}
		return "redirect:/products";
	}

}
