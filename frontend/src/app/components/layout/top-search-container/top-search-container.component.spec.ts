import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopSearchContainerComponent } from './top-search-container.component';

describe('TopSearchContainerComponent', () => {
  let component: TopSearchContainerComponent;
  let fixture: ComponentFixture<TopSearchContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopSearchContainerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TopSearchContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
