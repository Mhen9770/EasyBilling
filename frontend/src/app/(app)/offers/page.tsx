'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { offersApi, type OfferRequest, type OfferResponse, type OfferType } from '@/lib/api/offers/offersApi';
import { useToastStore } from '@/components/ui/toast';
import { Card, CardHeader, CardTitle, CardContent, Button, Input, Select, Badge, Modal, Table, type Column } from '@/components/ui';
import { PageLoader } from '@/components/ui/Loader';

export default function OffersPage() {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingOffer, setEditingOffer] = useState<OfferResponse | null>(null);
  const { addToast } = useToastStore();
  const queryClient = useQueryClient();

  const { data: offersData, isLoading } = useQuery({
    queryKey: ['offers'],
    queryFn: () => offersApi.listOffers(0, 100),
  });

  const offers = offersData?.data?.content || [];

  const createMutation = useMutation({
    mutationFn: (data: OfferRequest) => offersApi.createOffer(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['offers'] });
      setIsCreateModalOpen(false);
      addToast('Offer created successfully', 'success');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: OfferRequest }) =>
      offersApi.updateOffer(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['offers'] });
      setEditingOffer(null);
      setIsCreateModalOpen(false);
      addToast('Offer updated successfully', 'success');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => offersApi.deleteOffer(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['offers'] });
      addToast('Offer deleted successfully', 'success');
    },
  });

  const activateMutation = useMutation({
    mutationFn: (id: string) => offersApi.activateOffer(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['offers'] });
      addToast('Offer activated', 'success');
    },
  });

  const pauseMutation = useMutation({
    mutationFn: (id: string) => offersApi.pauseOffer(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['offers'] });
      addToast('Offer paused', 'success');
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);

    const offerData: OfferRequest = {
      name: formData.get('name') as string,
      description: formData.get('description') as string,
      type: formData.get('type') as OfferType,
      discountValue: parseFloat(formData.get('discountValue') as string),
      minPurchaseAmount: parseFloat(formData.get('minPurchaseAmount') as string) || undefined,
      maxDiscountAmount: parseFloat(formData.get('maxDiscountAmount') as string) || undefined,
      validFrom: formData.get('validFrom') as string || undefined,
      validTo: formData.get('validTo') as string || undefined,
      usageLimit: parseInt(formData.get('usageLimit') as string) || undefined,
      canStack: formData.get('canStack') === 'true',
      priority: parseInt(formData.get('priority') as string) || 0,
    };

    if (editingOffer) {
      updateMutation.mutate({ id: editingOffer.id, data: offerData });
    } else {
      createMutation.mutate(offerData);
    }
  };

  const columns: Column<OfferResponse>[] = [
    {
      key: 'name',
      header: 'Offer Name',
      sortable: true,
      render: (offer) => (
        <div>
          <div className="font-medium text-gray-900">{offer.name}</div>
          <div className="text-sm text-gray-500">{offer.description}</div>
        </div>
      ),
    },
    {
      key: 'type',
      header: 'Type',
      render: (offer) => <Badge variant="primary">{offer.type.replace(/_/g, ' ')}</Badge>,
    },
    {
      key: 'discountValue',
      header: 'Discount',
      render: (offer) => (
        <span className="font-semibold text-green-600">
          {offer.type === 'PERCENTAGE' ? `${offer.discountValue}%` : `₹${offer.discountValue}`}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (offer) => (
        <Badge
          variant={
            offer.status === 'ACTIVE' ? 'success' :
            offer.status === 'PAUSED' ? 'warning' :
            offer.status === 'EXPIRED' ? 'danger' :
            'default'
          }
        >
          {offer.status}
        </Badge>
      ),
    },
    {
      key: 'usageCount',
      header: 'Usage',
      render: (offer) => (
        <div className="text-sm">
          {offer.usageCount} {offer.usageLimit ? `/ ${offer.usageLimit}` : ''}
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (offer) => (
        <div className="flex gap-2">
          {offer.status === 'DRAFT' || offer.status === 'PAUSED' ? (
            <Button size="sm" variant="success" onClick={() => activateMutation.mutate(offer.id)}>
              Activate
            </Button>
          ) : offer.status === 'ACTIVE' ? (
            <Button size="sm" variant="warning" onClick={() => pauseMutation.mutate(offer.id)}>
              Pause
            </Button>
          ) : null}
          <Button size="sm" variant="ghost" onClick={() => { setEditingOffer(offer); setIsCreateModalOpen(true); }}>
            Edit
          </Button>
          <Button size="sm" variant="danger" onClick={() => {
            if (confirm(`Delete offer "${offer.name}"?`)) {
              deleteMutation.mutate(offer.id);
            }
          }}>
            Delete
          </Button>
        </div>
      ),
    },
  ];

  if (isLoading) {
    return <PageLoader text="Loading offers..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Offers & Promotions</h1>
          <p className="text-gray-600 mt-1">Create and manage promotional offers</p>
        </div>
        <Button variant="primary" onClick={() => setIsCreateModalOpen(true)} icon="➕">
          Create Offer
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <Card padding="sm">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{offers.length}</div>
            <div className="text-xs text-gray-600">Total Offers</div>
          </div>
        </Card>
        <Card padding="sm">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {offers.filter((o: OfferResponse) => o.status === 'ACTIVE').length}
            </div>
            <div className="text-xs text-gray-600">Active</div>
          </div>
        </Card>
        <Card padding="sm">
          <div className="text-center">
            <div className="text-2xl font-bold text-yellow-600">
              {offers.filter((o: OfferResponse) => o.status === 'PAUSED').length}
            </div>
            <div className="text-xs text-gray-600">Paused</div>
          </div>
        </Card>
        <Card padding="sm">
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {offers.reduce((sum: number, o: OfferResponse) => sum + o.usageCount, 0)}
            </div>
            <div className="text-xs text-gray-600">Total Redemptions</div>
          </div>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Offers</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <Table
            data={offers}
            columns={columns}
            keyExtractor={(offer) => offer.id}
            hover
            striped
          />
        </CardContent>
      </Card>

      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => { setIsCreateModalOpen(false); setEditingOffer(null); }}
        title={editingOffer ? 'Edit Offer' : 'Create New Offer'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            name="name"
            label="Offer Name"
            placeholder="e.g., Summer Sale"
            required
            defaultValue={editingOffer?.name}
            fullWidth
          />

          <Input
            name="description"
            label="Description"
            placeholder="Brief description"
            defaultValue={editingOffer?.description}
            fullWidth
          />

          <div className="grid grid-cols-2 gap-4">
            <Select
              name="type"
              label="Offer Type"
              options={[
                { value: 'PERCENTAGE', label: 'Percentage Discount' },
                { value: 'FIXED_AMOUNT', label: 'Fixed Amount' },
                { value: 'BUY_X_GET_Y', label: 'Buy X Get Y' },
                { value: 'BUNDLE', label: 'Bundle Offer' },
                { value: 'MINIMUM_PURCHASE', label: 'Minimum Purchase' },
                { value: 'CASHBACK', label: 'Cashback' },
              ]}
              defaultValue={editingOffer?.type || 'PERCENTAGE'}
              required
              fullWidth
            />

            <Input
              name="discountValue"
              label="Discount Value"
              type="number"
              step="0.01"
              placeholder="e.g., 10"
              required
              defaultValue={editingOffer?.discountValue}
              fullWidth
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              name="minPurchaseAmount"
              label="Minimum Purchase"
              type="number"
              step="0.01"
              placeholder="Optional"
              defaultValue={editingOffer?.minPurchaseAmount}
              fullWidth
            />

            <Input
              name="maxDiscountAmount"
              label="Maximum Discount"
              type="number"
              step="0.01"
              placeholder="Optional"
              defaultValue={editingOffer?.maxDiscountAmount}
              fullWidth
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              name="validFrom"
              label="Valid From"
              type="date"
              defaultValue={editingOffer?.validFrom?.split('T')[0]}
              fullWidth
            />

            <Input
              name="validTo"
              label="Valid To"
              type="date"
              defaultValue={editingOffer?.validTo?.split('T')[0]}
              fullWidth
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              name="usageLimit"
              label="Usage Limit"
              type="number"
              placeholder="Unlimited if empty"
              defaultValue={editingOffer?.usageLimit}
              fullWidth
            />

            <Input
              name="priority"
              label="Priority"
              type="number"
              placeholder="0 = lowest"
              defaultValue={editingOffer?.priority || 0}
              fullWidth
            />
          </div>

          <Select
            name="canStack"
            label="Can Stack with Other Offers?"
            options={[
              { value: 'true', label: 'Yes' },
              { value: 'false', label: 'No' },
            ]}
            defaultValue={editingOffer?.canStack ? 'true' : 'false'}
            fullWidth
          />

          <div className="flex gap-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => { setIsCreateModalOpen(false); setEditingOffer(null); }} fullWidth>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={createMutation.isPending || updateMutation.isPending} fullWidth>
              {editingOffer ? 'Update Offer' : 'Create Offer'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
