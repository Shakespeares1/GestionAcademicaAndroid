package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class ReportePageDto<T> {
    @SerializedName("items") public List<T> items;
    @SerializedName("page") public int page;
    @SerializedName("size") public int size;
    @SerializedName("totalElements") public long totalElements;
    @SerializedName("totalPages") public int totalPages;
}