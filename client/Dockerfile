# Stage 1: Install dependencies
FROM node:18-alpine AS deps
RUN apk add --no-cache git
RUN corepack enable && corepack prepare pnpm@9 --activate
ARG NPM_REGISTRY=https://registry.npmjs.org
RUN pnpm config set registry ${NPM_REGISTRY}
WORKDIR /app
COPY package*.json ./
RUN pnpm install

# Stage 2: Build (with more memory)
FROM node:18-alpine AS build
RUN corepack enable && corepack prepare pnpm@9 --activate
# Configure pnpm to use HTTPS for Git operations
RUN pnpm config set git-checks false
RUN pnpm config set strict-ssl false

WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ENV NODE_OPTIONS="--max-old-space-size=4096"
RUN pnpm run build

# Stage 3: Deploy
FROM nginx:alpine

# Install envsubst for template processing
RUN apk add --no-cache gettext
# Remove default config
RUN rm /etc/nginx/conf.d/default.conf
# Copy template config
COPY nginx.conf /etc/nginx/conf.d/

COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
