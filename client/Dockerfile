# frontend/Dockerfile
FROM node:18 as build
WORKDIR /app
COPY package*.json ./

ARG NPM_REGISTRY=https://registry.npmjs.org
RUN npm config set registry ${NPM_REGISTRY}

RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
