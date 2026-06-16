import os
import re

base_dir = r"C:\Users\tunga\Desktop\SportShoe-Store\BE\server\src\main\java\com\example\server"
repos_dir = os.path.join(base_dir, "repository")
services_dir = os.path.join(base_dir, "core", "admin", "quanLyDanhMuc")

attributes = [
    ("ChatLieuGiay", "Ten"),
    ("CoGiay", "Ten"),
    ("CongNgheDem", "Ten"),
    ("DeGiay", "Ten"),
    ("KichCo", "GiaTri"),
    ("LoaiGiay", "Ten"),
    ("MauSac", "Ten"),
    ("ThuongHieu", "Ten"),
    ("TrongLuong", "GiaTri")
]

for entity, field in attributes:
    repo_file = os.path.join(repos_dir, f"{entity}Repository.java")
    if os.path.exists(repo_file):
        with open(repo_file, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Add findBy method if not exists
        find_method = f"Optional<{entity}> findBy{field}IgnoreCase(String {field.lower()});" if field != "GiaTri" or entity == "KichCo" else f"Optional<{entity}> findByGiaTri(Integer giaTri);"
        if "java.util.Optional" not in content and "import java.util.Optional;" not in content:
            content = content.replace("public interface", "import java.util.Optional;\n\npublic interface")
        
        if find_method.split("(")[0] not in content:
            content = content.replace("}", f"    {find_method}\n}}")
            with open(repo_file, "w", encoding="utf-8") as f:
                f.write(content)
                print(f"Updated {entity}Repository")

    service_file = os.path.join(services_dir, entity[:1].lower() + entity[1:], "service", "impl" if entity not in ["TrongLuong", "MauSac", "KichCo", "DeGiay", "CongNgheDem", "CoGiay", "ChatLieuGiay", "ThuongHieu", "LoaiGiay"] else "service", f"{entity}Service.java")
    # Actually wait, most services don't have impl folder anymore? Or they do?
    if not os.path.exists(service_file):
        service_file = os.path.join(services_dir, entity[:1].lower() + entity[1:], "service", f"{entity}Service.java")
    if not os.path.exists(service_file):
        service_file = os.path.join(services_dir, entity[:1].lower() + entity[1:], "service", "impl", f"{entity}ServiceImpl.java")
        
    if os.path.exists(service_file):
        with open(service_file, "r", encoding="utf-8") as f:
            content = f.read()

        # We will replace the existsBy check in tao... method with a findBy check
        # Example for Ten:
        # if (repository.existsByTenIgnoreCase(req.ten())) { throw new BusinessException(...); }
        
        # First let's print the service file path to be sure
        print(f"Found {service_file}")
