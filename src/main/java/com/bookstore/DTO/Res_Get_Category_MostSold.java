package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Res_Get_Category_MostSold {
    private String categoryId;
    private String categoryName;
    private List<Res_Get_Books> books = new ArrayList<>();
}
