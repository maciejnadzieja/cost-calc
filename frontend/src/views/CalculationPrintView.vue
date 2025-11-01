<template>
  <div class="container my-4">
    <!-- Print button -->
    <div class="no-print mb-4">
      <button @click="window.print()" class="btn btn-primary">
        <i class="bi bi-printer"></i> <span>Print</span>
      </button>
      <router-link to="/calculations" class="btn btn-secondary">
        <i class="bi bi-arrow-left"></i> <span>Back</span>
      </router-link>
    </div>

    <!-- Calculation header -->
    <div class="calculation-header">
      <h1>{{ calculation.name }}</h1>
      <p class="text-muted">{{ calculation.description }}</p>
    </div>

    <!-- Stages -->
    <div v-for="stage in calculation.stages" :key="stage.id" class="stage">
      <h2 class="stage-title">{{ stage.name }}</h2>

      <!-- Services -->
      <div v-if="stage.services && stage.services.length" class="mb-4">
        <h3>Services</h3>
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Item</th>
              <th>Quantity</th>
              <th>Unit</th>
              <th>Unit Price</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="service in stage.services" :key="service.id">
              <td>{{ service.name }}</td>
              <td>{{ service.quantity }}</td>
              <td>{{ service.unit }}</td>
              <td>{{ formatCurrency(service.unitPrice) }}</td>
              <td>{{ formatCurrency(service.quantity * service.unitPrice) }}</td>
            </tr>
            <tr class="stage-total">
              <td colspan="4">Stage Total</td>
              <td>{{ formatCurrency(stageServicesTotal(stage)) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Materials -->
      <div v-if="stage.materials && stage.materials.length" class="mb-4">
        <h3>Materials</h3>
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Item</th>
              <th>Quantity</th>
              <th>Unit</th>
              <th>Unit Price</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="material in stage.materials" :key="material.id">
              <td>{{ material.name }}</td>
              <td>{{ material.quantity }}</td>
              <td>{{ material.unit }}</td>
              <td>{{ formatCurrency(material.unitPrice) }}</td>
              <td>{{ formatCurrency(material.quantity * material.unitPrice) }}</td>
            </tr>
            <tr class="stage-total">
              <td colspan="4">Stage Total</td>
              <td>{{ formatCurrency(stageMaterialsTotal(stage)) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Stage total -->
      <div class="stage-total p-2 mb-4">
        <strong>Stage Total</strong>:
        <span>{{ formatCurrency(stageTotal(stage)) }}</span>
      </div>
    </div>

    <!-- Calculation total -->
    <div class="total">
      <span>Total</span>:
      <span>{{ formatCurrency(calculation.total) }}</span>
    </div>

    <!-- Footer -->
    <div class="mt-5 pt-4 border-top">
      <div class="text-muted small">
        <pre>{{ userSettings.footerText }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const calculation = ref({
  name: 'Project Alpha',
  description: 'This is a sample project calculation.',
  currency: 'USD',
  stages: [
    {
      id: 1,
      name: 'Stage 1: Foundation',
      services: [
        { id: 1, name: 'Excavation', quantity: 10, unit: 'h', unitPrice: 150 },
        { id: 2, name: 'Concrete Pouring', quantity: 20, unit: 'm3', unitPrice: 200 },
      ],
      materials: [
        { id: 1, name: 'Rebar', quantity: 100, unit: 'm', unitPrice: 5 },
        { id: 2, name: 'Gravel', quantity: 50, unit: 't', unitPrice: 30 },
      ],
    },
    {
      id: 2,
      name: 'Stage 2: Framing',
      services: [
        { id: 3, name: 'Wall Framing', quantity: 40, unit: 'h', unitPrice: 50 },
      ],
      materials: [
        { id: 3, name: 'Lumber 2x4', quantity: 200, unit: 'pc', unitPrice: 10 },
        { id: 4, name: 'Nails', quantity: 5, unit: 'kg', unitPrice: 15 },
      ],
    },
  ],
  total: 0, // Will be calculated
});

const userSettings = ref({
  footerText: 'Thank you for your business!\nPayment is due within 30 days.',
});

const formatCurrency = (value: number) => {
  return `${value.toFixed(2)} ${calculation.value.currency}`;
};

const stageServicesTotal = (stage: any) => {
  return stage.services.reduce((total: number, service: any) => total + service.quantity * service.unitPrice, 0);
};

const stageMaterialsTotal = (stage: any) => {
  return stage.materials.reduce((total: number, material: any) => total + material.quantity * material.unitPrice, 0);
};

const stageTotal = (stage: any) => {
  return stageServicesTotal(stage) + stageMaterialsTotal(stage);
};

calculation.value.total = calculation.value.stages.reduce((total: number, stage: any) => total + stageTotal(stage), 0);

</script>

<style scoped>
@media print {
  .no-print {
    display: none;
  }
  body {
    padding: 0;
    margin: 0;
  }
  .container {
    max-width: 100%;
    width: 100%;
    padding: 0;
    margin: 0;
  }
  .table {
    font-size: 12px;
  }
  .stage-total {
    font-weight: bold;
    background-color: #f8f9fa !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
.logo {
  max-width: 200px;
  height: auto;
  margin-bottom: 1rem;
}
.calculation-header {
  margin-bottom: 2rem;
}
.stage {
  margin-bottom: 2rem;
}
.stage-title {
  color: #198754;
  border-bottom: 2px solid #198754;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
}
.table th {
  background-color: #e9ecef;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
}
.total {
  font-size: 1.2em;
  font-weight: bold;
  margin-top: 2rem;
  text-align: right;
}
</style>
