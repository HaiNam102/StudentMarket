package com.postgresql.StudentMarket.Dto;

import java.util.List;

public class ParentWithChildren {
  public Integer parentId;
  public String name;

  // 🔴 THÊM FIELD NÀY
  public String iconPath;

  public List<ChildItem> children;

  public static class ChildItem {
    public Integer childId;
    public String name;
    public ChildItem(Integer id, String name){ this.childId = id; this.name = name; }
  }

  public ParentWithChildren() {} // nên có để JPA/mapper hoặc Jackson tiện dùng

  // constructor cũ vẫn giữ
  public ParentWithChildren(Integer id, String name, List<ChildItem> kids) {
    this.parentId = id; this.name = name; this.children = kids;
  }

  // overload nếu bạn muốn set icon luôn khi map
  public ParentWithChildren(Integer id, String name, String iconPath, List<ChildItem> kids) {
    this.parentId = id; this.name = name; this.iconPath = iconPath; this.children = kids;
  }
}
