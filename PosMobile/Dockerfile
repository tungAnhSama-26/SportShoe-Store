# Stage 1: Build Expo Web
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npx expo export -p web

# Stage 2: Serve bang Nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]