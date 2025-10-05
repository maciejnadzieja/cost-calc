# Cost Calculator Web Application Planning Document

## Overview

The "Cost Calc" application will be a web-based tool that helps small businesses (particularly service providers like gardeners, plumbers, cleaners, etc.) to:
1. Define their pricing structures
2. Create detailed cost calculations for clients
3. Generate professional estimates/quotes

## Target Users

- Small business owners and independent professionals who need to create cost calculations
- Service-based businesses like gardening, landscaping, cleaning, plumbing, etc.
- Craftspeople and tradespeople who provide materials + labor quotes

## Core Features (MVP)

1. **User Authentication**
   - Secure login/registration
   - Business profile creation

2. **Service & Material Catalog**
   - Create and maintain a catalog of services with pricing
   - Add materials with costs, markups, and units
   - Group services and materials into categories

3. **Client Management**
   - Store client information
   - Access client history and previous quotes

4. **Cost Calculation Engine**
   - Select services and materials for a specific job
   - Add quantities
   - Apply discounts or special rates
   - Include taxes
   - Calculate totals and profit margins

5. **Estimate Generation**
   - Create professional-looking estimates in PDF format
   - Send estimates via email
   - Allow for client approval/electronic signature

6. **Basic Reporting**
   - View calculation history
   - Basic analytics on quotes vs. conversions

## Technical Stack

### Backend
- **Kotlin**: Core programming language
- **Web Framework**: Spring boot with spring web
- **Database**: Mongodb
- **Authentication**: Spring security

### Frontend
- **HTML/CSS/JavaScript**: Core web technologies
- **Framework**: 
   - Option 1: Minimal JS with Bootstrap for responsive design
   - Option 2: React for more interactive UI
- **CSS Framework**: Bootstrap CSS

### Deployment
- **Development**: Local development environment
- **Docker**: provide docker configuration to create container with application

## User Flow

1. User registers/logs in
2. Sets up business profile and default rates
3. Creates catalog of services and materials
4. Adds client information
5. Creates new calculation
6. Selects client
7. Adds services and materials with quantities
8. Reviews and adjusts calculation
9. Generates and sends estimate to client
10. Updates status based on client response

## Future Enhancements (Post-MVP)

- Mobile app or responsive mobile interface
- Online payment integration
- Scheduling and calendar integration
- Inventory management
- Project management features
- Advanced reporting and analytics
- Integration with accounting software
- Multiple user accounts for larger businesses
- Templates for common job types

## Similar Applications Research

Based on research, similar applications in the market include:

1. **Jobber**: Comprehensive service business management
2. **ServiceM8**: Field service management with job costing
3. **QuickBooks Field Service**: Estimate and invoicing for service businesses
4. **Housecall Pro**: Service business management platform

Key differentiators for our application:
- Simpler, more focused on accurate cost calculations
- More affordable for very small businesses
- Customizable for specific industry needs
- Open-source and self-hostable option

## Limitations and Considerations

- Need careful handling of sensitive client and pricing data
- May require industry-specific customizations
- Will need to consider various tax structures in different regions
- Mobile support important for on-site estimating
- Learning curve for non-technical business owners