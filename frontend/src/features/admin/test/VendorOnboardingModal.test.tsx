import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { VendorOnboardingModal } from '../components/VendorOnboardingModal';

describe('VendorOnboardingModal', () => {
  it('renders modal fields when open', () => {
    render(
      <VendorOnboardingModal
        isOpen={true}
        onClose={vi.fn()}
        onSubmit={vi.fn()}
      />
    );

    expect(screen.getByText('Onboard New Vendor')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('e.g. Acme Charge Tech')).toBeInTheDocument();
  });

  it('submits onboarding form data correctly', async () => {
    const handleSubmit = vi.fn().mockResolvedValue(undefined);
    render(
      <VendorOnboardingModal
        isOpen={true}
        onClose={vi.fn()}
        onSubmit={handleSubmit}
      />
    );

    fireEvent.change(screen.getByPlaceholderText('e.g. Acme Charge Tech'), {
      target: { value: 'Test Charge Corp' },
    });
    fireEvent.change(screen.getByPlaceholderText('e.g. REG-991823'), {
      target: { value: 'REG-123456' },
    });
    fireEvent.change(screen.getByPlaceholderText('contact@vendor.com'), {
      target: { value: 'info@testcharge.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), {
      target: { value: 'John Smith' },
    });
    fireEvent.change(screen.getByPlaceholderText('admin@vendor.com'), {
      target: { value: 'admin@testcharge.com' },
    });

    fireEvent.submit(screen.getByRole('button', { name: /send vendor invitation/i }));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: 'Test Charge Corp',
      businessRegistrationNumber: 'REG-123456',
      contactEmail: 'info@testcharge.com',
      adminFullName: 'John Smith',
      adminEmail: 'admin@testcharge.com',
    });
  });
});
