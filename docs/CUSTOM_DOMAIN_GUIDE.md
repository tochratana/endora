# Custom Domain Setup Guide

## 🌐 Setting Up Your Custom Domain for Endora API

You have several options to use your own domain name instead of the default Cloud Run URL.

### Current Service URL
- **Default**: https://api-engine-backend-308354822720.us-central1.run.app/

### Option 1: Google Cloud Domain Mapping (Recommended)

This is the official Google Cloud method that provides:
- ✅ Free SSL certificates (automatically managed)
- ✅ Global CDN integration
- ✅ Built-in security features
- ✅ Easy management through gcloud CLI

#### Prerequisites:
1. **Own a domain** (e.g., yourdomain.com)
2. **Access to DNS management** for your domain
3. **Domain verification** in Google Search Console

#### Steps:

1. **Run the setup script:**
   ```bash
   ./setup-custom-domain.sh
   ```

2. **The script will guide you through:**
   - Domain verification
   - Creating the domain mapping
   - Getting DNS records to configure

3. **Add DNS records** provided by the script to your domain provider

#### Example Domain Options:
- `api.yourdomain.com` (subdomain for API)
- `endora.yourdomain.com` (branded subdomain)
- `yourdomain.com` (use main domain)

### Option 2: Cloudflare Proxy (Alternative)

If you use Cloudflare for DNS:

1. **Add a CNAME record:**
   ```
   Type: CNAME
   Name: api (or your preferred subdomain)
   Target: api-engine-backend-308354822720.us-central1.run.app
   ```

2. **Enable Cloudflare features:**
   - SSL/TLS encryption
   - DDoS protection
   - Caching rules

### Option 3: Load Balancer with Custom Domain

For enterprise setups:
- Google Cloud Load Balancer
- Custom SSL certificates
- Advanced routing rules

## 🚀 Quick Setup

### Method 1: Automated Script
```bash
./setup-custom-domain.sh
```

### Method 2: Manual Setup
```bash
# Replace 'api.yourdomain.com' with your domain
gcloud run domain-mappings create \
    --service=api-engine-backend \
    --domain=api.yourdomain.com \
    --region=us-central1 \
    --project=api-engine-backend-20250829
```

## 📋 DNS Configuration Examples

### For api.yourdomain.com:
```
Type: CNAME
Name: api
Target: ghs.googlehosted.com
```

### For yourdomain.com (apex domain):
```
Type: A
Name: @
Target: [IP provided by Google]
```

## ⏱️ Timeline
- **Domain mapping creation**: Immediate
- **DNS propagation**: 24-48 hours
- **SSL certificate**: Automatic after DNS is active

## 🔍 Verification

After DNS propagation, test your domain:
```bash
curl https://yourdomain.com/
```

Should return the same response as the original URL.

## 🛠️ Troubleshooting

### Common Issues:
1. **DNS not propagated**: Wait longer or check with `dig yourdomain.com`
2. **SSL certificate pending**: Automatic once DNS is verified
3. **Domain verification failed**: Check Google Search Console

### Check Status:
```bash
gcloud run domain-mappings describe yourdomain.com \
    --region=us-central1 \
    --project=api-engine-backend-20250829
```

## 💡 Recommendations

- **Use a subdomain** like `api.yourdomain.com` for better organization
- **Verify domain ownership** in Google Search Console first
- **Keep the original URL** as a backup during transition
- **Update your frontend applications** to use the new domain
- **Set up monitoring** for the custom domain

## 🔐 Security Notes

- SSL certificates are automatically managed by Google Cloud
- All traffic is encrypted by default
- Keep your domain provider account secure
- Monitor domain expiration dates
