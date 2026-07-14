# Dùng Nginx để serve file tĩnh
FROM nginx:alpine
COPY build /usr/share/nginx/html
# Copy cấu hình nginx tùy chỉnh (hỗ trợ Vue Router history mode)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
