FROM nginx:alpine
COPY build /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html
COPY nginx.local.conf /etc/nginx/conf.d/default.conf
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
