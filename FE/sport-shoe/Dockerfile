# Stage 1: Build source code VueJS / Vite
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN NODE_OPTIONS="--max-old-space-size=2048" npm run build

# Stage 2: Dùng Nginx để serve file tĩnh
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
# Copy cấu hình nginx tùy chỉnh (hỗ trợ Vue Router history mode)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
