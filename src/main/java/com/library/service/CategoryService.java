package com.library.service;

import com.library.dao.CategoryDAO;
import com.library.model.Category;
import com.library.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    public Category getCategoryById(int id) throws SQLException {
        return categoryDAO.getById(id);
    }

    public boolean saveCategory(Category category) throws SQLException {
        if (!ValidationUtil.isNotEmpty(category.getCategoryName())) {
            throw new IllegalArgumentException("Category name is required.");
        }

        if (category.getCategoryId() > 0) {
            return categoryDAO.updateCategory(category);
        } else {
            return categoryDAO.addCategory(category);
        }
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        return categoryDAO.deleteCategory(categoryId);
    }
}
