# Dùng Nginx để serve file tĩnh
FROM nginx:alpine
COPY build /usr/share/nginx/html
# Cấp quyền đọc ghi cho Nginx đối với toàn bộ file tĩnh
RUN chmod -R 755 /usr/share/nginx/html
# Copy cấu hình nginx tùy chỉnh (hỗ trợ Vue Router history mode)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
